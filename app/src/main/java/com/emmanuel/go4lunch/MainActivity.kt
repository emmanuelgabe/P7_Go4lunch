package com.emmanuel.go4lunch

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.databinding.ActivityMainBinding
import com.emmanuel.go4lunch.databinding.ActivityMainDrawerHeaderBinding
import com.emmanuel.go4lunch.ui.listview.ListViewFragmentDirections
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
            R.id.yourLunch -> {
                fetchWorkmateFavorite()
            }
            R.id.settingsFragment -> {
                val action =
                    MainNavigationDirections.actionGlobalSettingsFragment()
                findNavController(this@MainActivity, R.id.nav_host_fragment).navigate(
                    action
                )
                binding.drawerLayout.close()
            }
        }
        return true
    }

    private fun fetchWorkmateFavorite() {
        CoroutineScope(IO).launch {
            val workmateList = WorkmateRepository.getAllWorkmate()
            var yourLunchId: String? = null
            for (workmate in workmateList) {
                if (workmate.uid.equals(mAuth.currentUser!!.uid))
                    yourLunchId = workmate.restaurantFavorite
            }
            withContext(Main) {
                if (!yourLunchId.isNullOrBlank()) {
                    val action =
                        MainNavigationDirections.actionGlobalRestaurantDetail(null, yourLunchId)
                    findNavController(this@MainActivity, R.id.nav_host_fragment)
                        .navigate(
                            action
                        )
                    binding.drawerLayout.close()
                } else {

                    Toast.makeText(
                        baseContext,
                        "You have not yet chosen a restaurant.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateUserProfile() {
        CoroutineScope(IO).launch {
            val currentWorkmate = async {
                WorkmateRepository.getUser(mAuth.currentUser!!.uid)
            }.await()
            withContext(Main) {
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
