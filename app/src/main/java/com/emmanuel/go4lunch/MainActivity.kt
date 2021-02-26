package com.emmanuel.go4lunch

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.databinding.ActivityMainBinding
import com.emmanuel.go4lunch.databinding.ActivityMainDrawerHeaderBinding
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mAuth: FirebaseAuth
    private lateinit var headerBinding: ActivityMainDrawerHeaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        headerBinding =
            ActivityMainDrawerHeaderBinding.bind(binding.drawerNavView.getHeaderView(0))
        val view = binding.root
        setContentView(view)

        mAuth = FirebaseAuth.getInstance()

        initUI()
    }

    private fun initUI() {
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        binding.bottomNavView.setupWithNavController(navController)
        binding.drawerNavView.setupWithNavController(navController)
        setSupportActionBar(binding.toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.mapViewFragment, R.id.listViewFragment, R.id.workmatesFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.drawerNavView.setNavigationItemSelectedListener(this)
        updateUserProfile()
    }


    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_item -> {
                mAuth.signOut()
                LoginManager.getInstance().logOut()
                startActivity(Intent(this, AuthenticationActivity::class.java))
                finish()
            }
        }
        return true
    }

    private fun updateUserProfile() {
        CoroutineScope(IO).launch {
            val currentWorkmate = async {
                WorkmateRepository.getUser(mAuth.currentUser!!.uid)
            }.await()
            withContext(Main){
                headerBinding.drawerHeaderUsernameTextView.text =
                    currentWorkmate.name
                headerBinding.drawerHeaderUserEmailTextView.text =
                    currentWorkmate.email
                Picasso.get()
                    .load(currentWorkmate.avatarURL)
                    .resize(60, 60)
                    .into(headerBinding.drawerHeaderUserImage)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
