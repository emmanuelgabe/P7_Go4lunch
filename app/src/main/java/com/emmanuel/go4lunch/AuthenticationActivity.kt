package com.emmanuel.go4lunch


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.ActivityAuthenticationBinding
import com.emmanuel.go4lunch.di.Injection
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.firebase.ui.auth.ErrorCodes.NO_NETWORK
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()

        initFacebookAuthentication()
        initGoogleAuthentication()
    }

    private fun initFacebookAuthentication() {
        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()
        binding.activityAuthenticationFacebookButton.setPermissions(
            listOf(
                "email",
                "public_profile"
            )
        )
        binding.activityAuthenticationFacebookButton.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })
    }

    private fun initGoogleAuthentication() {
        // Configure Google Sign-In to request the user data required
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.activityAuthenticationGoogleButton.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // callback ActivityResult  from Facebook sdk
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            when {
                resultCode == RESULT_OK -> {
                    try {
                        val account = task.getResult(ApiException::class.java)!!
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        Log.w(TAG, "Google sign in failed", e)
                    }
                }
                response == null -> {
                    showAlertDialog(getString(R.string.authentication_activity_error_unknown))
                }
                response.error?.errorCode == NO_NETWORK -> {
                    showAlertDialog(getString(R.string.error_no_internet))
                }
            }
        }
    }

    /**
     * After a user successfully signs in, get an ID token from the GoogleSignInAccount object,
     * exchange it for a Firebase credential, and authenticate with Firebase using the Firebase credential:
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val acct: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
                    Log.d(TAG, "signInWithCredential:success")
                    addNewUser(
                        Workmate(
                            mAuth.currentUser?.uid.toString(),
                            acct?.email.toString(),
                            mAuth.currentUser?.displayName.toString(),
                            mAuth.currentUser?.photoUrl.toString()
                        )
                    )
                    updateUI(mAuth.currentUser)
                } else {
                    showAlertDialog(getString(R.string.error_authentication_failed))
                }
            }
    }

    /**
     *  Call after user give permission to access his data on Facebook
     *  The token is use to generate a credential to login to firebase on your database users
     */
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addNewUser(
                        Workmate(
                            mAuth.currentUser?.uid.toString(),
                            mAuth.currentUser?.email.toString(),
                            mAuth.currentUser?.displayName.toString(),
                            mAuth.currentUser?.photoUrl.toString()
                        )
                    )
                    updateUI(mAuth.currentUser)
                } else {
                    Log.w(TAG, getString(R.string.error_authentication_failed), task.exception)
                    showAlertDialog(getString(R.string.error_authentication_failed))
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            binding.activityAuthenticationFacebookButton.isClickable = false
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun addNewUser(workmate: Workmate) {
        /**
         *  email can be null or blank if user if the access permissions is denied or if the email
         *  of the account is not verified
         */
         if (mAuth.currentUser?.email.isNullOrBlank()) {
            workmate.email = "${mAuth.currentUser?.displayName}@Facebook.com".replace("\\s+", "")
        }
        CoroutineScope(IO).launch {
            val workmateRepository = Injection.provideWorkmateDataSource()
            workmateRepository.createWorkmate(workmate)
        }
    }

    override fun onStart() {
        super.onStart()
        updateUI(mAuth.currentUser)

    }

    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setTitle(resources.getString(R.string.alert_dialog_title))
        builder.setNeutralButton(getString(R.string.alert_dialog_neutral_button)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    companion object {
        const val TAG = "Authentication activity"
        private const val RC_GOOGLE_SIGN_IN = 5000
    }
}