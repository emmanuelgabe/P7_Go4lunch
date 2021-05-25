package com.emmanuel.go4lunch

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
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
import androidx.preference.PreferenceManager
import com.emmanuel.go4lunch.databinding.ActivityMainBinding
import com.emmanuel.go4lunch.databinding.ActivityMainDrawerHeaderBinding
import com.emmanuel.go4lunch.di.ViewModelFactory
import com.emmanuel.go4lunch.ui.settings.SettingsFragment
import com.emmanuel.go4lunch.utils.CircleTransform
import com.emmanuel.go4lunch.utils.FetchLocationEvent
import com.emmanuel.go4lunch.utils.ResetSearchView
import com.emmanuel.go4lunch.utils.hideKeyboard
import com.facebook.login.LoginManager
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Inject lateinit var factory: ViewModelFactory
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var headerBinding: ActivityMainDrawerHeaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.app().appComponent.inject(this)
        mainViewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
        binding = ActivityMainBinding.inflate(layoutInflater)
        headerBinding =
            ActivityMainDrawerHeaderBinding.bind(binding.drawerNavView.getHeaderView(0))
        val view = binding.root
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        setContentView(view)
        initUI()
    }

    private fun initUI() {
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        binding.bottomNavView.setupWithNavController(navController)
        binding.drawerNavView.setupWithNavController(navController)
        setSupportActionBar(binding.toolbar)

        binding.toolbarOpenSearchButton.setOnClickListener {
            binding.toolbarSearchContainer.slideRight()
            binding.toolbarOpenSearchButton.visibility = View.GONE
        }

        binding.searchAutoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {}
            override fun beforeTextChanged(text: CharSequence?,start: Int,count: Int,after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                if (text?.isEmpty() == true) {
                    binding.searchAutoCompleteTextView.setAdapter(null)
                }
                mainViewModel.setInputTextSearch(binding.searchAutoCompleteTextView.text.toString())
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val radiusPreference = sharedPreferences.getInt(SettingsFragment.KEY_PREF_RESTAURANT_RADIUS,1000)
                mainViewModel.getPlaces(binding.searchAutoCompleteTextView.text.toString(),radiusPreference)
            }
        })
        binding.searchAutoCompleteTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                updateUiAfterSearch()
            }
            true
        }
        binding.toolbarValidSearchButton.setOnClickListener {
            updateUiAfterSearch()
        }
        binding.toolbarClearSearchButton.setOnClickListener {
            resetSearch()

        }
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.mapViewFragment, R.id.listViewFragment, R.id.workmatesFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.drawerNavView.setNavigationItemSelectedListener(this)
        mainViewModel.addWorkmatesSnapshotListener()

        mainViewModel.currentUserLiveData.observe(this, { currentUser ->
            headerBinding.drawerHeaderUsernameTextView.text =
                currentUser!!.name
            headerBinding.drawerHeaderUserEmailTextView.text =
                currentUser.email
            Picasso.get()
                .load(currentUser.avatarURL)
                .transform(CircleTransform())
                .resize(60, 60)
                .into(headerBinding.drawerHeaderUserImage)
        })
        mainViewModel.placesAutocompleteLiveData.observe(this, { restaurantsPlaceSearch ->
            val restaurantPlaceName = mutableListOf<String>()
            for (restaurant in restaurantsPlaceSearch) {
                if (restaurant.types.contains("restaurant"))
                    restaurantPlaceName.add(restaurant.structuredFormatting.mainText)
            }

            binding.searchAutoCompleteTextView.setAdapter(
                ArrayAdapter(
                    this, android.R.layout.simple_dropdown_item_1line,
                    restaurantPlaceName
                )
            )
        })
    }

    private fun resetSearch() {
        binding.searchAutoCompleteTextView.setText("")
        mainViewModel.textSearchInput.value = ""
        mainViewModel.placesAutocompleteLiveData.value = listOf()
        updateUiAfterSearch()
        binding.toolbarSearchContainer.slideLeft()
        binding.toolbarOpenSearchButton.visibility = View.VISIBLE
    }

    private fun updateUiAfterSearch() {
        binding.searchAutoCompleteTextView.setAdapter(null)
        hideKeyboard()
        binding.searchAutoCompleteTextView.clearFocus()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_item -> {
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                startActivity(Intent(this, AuthenticationActivity::class.java))
                finish()
            }
            R.id.yourLunch -> {
                if (!mainViewModel.currentUserLiveData.value?.restaurantFavorite.isNullOrBlank()) {
                    val action =
                        MainNavigationDirections.actionGlobalRestaurantDetail(
                            mainViewModel.currentUserLiveData.value!!.restaurantFavorite
                        )
                    findNavController(this@MainActivity, R.id.nav_host_fragment)
                        .navigate(
                            action
                        )
                    binding.drawerLayout.close()
                } else {

                    Toast.makeText(
                        baseContext,
                        getString(R.string.drawer_toast_message_no_launch),
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

    private fun View.slideRight() {
        visibility = View.VISIBLE
        val animate = TranslateAnimation(this.width.toFloat(), 0f, 0f, 0f)
        animate.duration = 500
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    private fun View.slideLeft() {
        visibility = View.GONE
        val animate = TranslateAnimation(0f, this.width.toFloat(), 0f, 0f)
        animate.duration = 500
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    @Subscribe
    fun fetchLocationEvent(event: FetchLocationEvent?) {
        registerLocationUpdate()
    }

    @Subscribe
    fun resetSearchEvent(event: ResetSearchView?) {
       if (binding.toolbarOpenSearchButton.visibility == View.GONE)
        resetSearch()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    /**
     * Register for location updates from [mFusedLocationClient], through callback on the main looper.
     * Once the location is available the device will be able to launch the query for get restaurant
     * detail near the current place.
     */
    private fun registerLocationUpdate() {
        if (locationCallback == null) {
            try {
                locationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 10
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        locationResult ?: return
                        if (locationResult.locations.isNotEmpty()) {
                            mainViewModel.saveLocation(locationResult.lastLocation)
                            mFusedLocationClient.removeLocationUpdates(locationCallback!!)
                            locationCallback = null
                        }
                    }
                }
                mFusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback!!,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }
    }
}
