package com.emmanuel.go4lunch.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.emmanuel.go4lunch.AuthenticationActivity
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var mAccountDialogPreference: Preference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        initUI()
        return view
    }

    private fun initUI() {
        mAccountDialogPreference = findPreference("dialog_preference_delete_account")
        mAccountDialogPreference.setOnPreferenceClickListener {
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.apply {
                setMessage("Are you sure you want to delete your account?\n All your data will be deleted.\n The removal will be irreversible")
                setTitle("Delete Account")
                setPositiveButton("Ok") { _, _ ->
                    mAuth = FirebaseAuth.getInstance()
                    val user = mAuth.currentUser
                    CoroutineScope(IO).launch {
                        launch {
                            WorkmateRepository.deleteWorkmate(user!!.uid)
                        }.join()
                        launch {
                            user!!.delete()
                        }.join()
                        withContext(Main) {
                            mAuth.signOut()
                            LoginManager.getInstance().logOut()
                            startActivity(
                                Intent(
                                    requireContext(),
                                    AuthenticationActivity::class.java
                                )
                            )
                            requireActivity().finish()
                            Toast.makeText(
                                requireContext(),
                                "Your account has been deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                setNegativeButton(getString(R.string.alert_dialog_permission_button_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            }
            val dialog = builder.create()
            dialog.show()
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_settings, rootKey)
    }
}