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
import com.emmanuel.go4lunch.di.Injection
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
                setMessage(getString(R.string.setting_fragment_dialog_message_account_delete))
                setTitle(getString(R.string.setting_fragment_dialog_title_account_delete))
                setPositiveButton(getString(R.string.setting_fragment_dialog_button_validate)) { _, _ ->
                    mAuth = FirebaseAuth.getInstance()
                    val user = mAuth.currentUser
                    CoroutineScope(IO).launch {
                        launch {
                            val workmateRepository = Injection.provideWorkmateDataSource()
                            workmateRepository.deleteWorkmate(user!!.uid)
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
                                getString(R.string.setting_fragment_message_account_delete),
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

    companion object {
        const val KEY_PREF_NOTIFICATION_PREFERENCE = "notification_preference"
        const val KEY_PREF_NOTIFICATION_HOUR_PREFERENCE = "notification_hour_preference"
        const val KEY_PREF_MAP_ZOOM = "map_zoom_preference"
        const val KEY_PREF_RESTAURANT_RADIUS = "restaurant_radius_preference"
    }
}