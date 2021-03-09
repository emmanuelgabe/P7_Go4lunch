package com.emmanuel.go4lunch

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.emmanuel.go4lunch.databinding.ActivityMainBinding
import com.emmanuel.go4lunch.databinding.ActivityMainDrawerHeaderBinding
import com.emmanuel.go4lunch.di.Injection
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mAuth: FirebaseAuth
    private lateinit var headerBinding: ActivityMainDrawerHeaderBinding
    private var factory = Injection.provideViewModelFactory()

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
    }

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

        mainViewModel.workmatesLiveData.observe(this, { currentUser ->
            headerBinding.drawerHeaderUsernameTextView.text =
                currentUser!!.name
            headerBinding.drawerHeaderUserEmailTextView.text =
                currentUser.email
            Picasso.get()
                .load(currentUser.avatarURL)
                .resize(60, 60)
                .into(headerBinding.drawerHeaderUserImage)
        })
        mainViewModel.getUser(mAuth.currentUser!!.uid)
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
                if (!mainViewModel.workmatesLiveData.value?.restaurantFavorite.isNullOrBlank()) {
                    val action =
                        MainNavigationDirections.actionGlobalRestaurantDetail(null, mainViewModel.workmatesLiveData.value!!.restaurantFavorite)
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
}
