package com.emmanuel.go4lunch

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
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
import com.emmanuel.go4lunch.utils.hideKeyboard
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

@Suppress("COMPATIBILITY_WARNING")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
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
        initUI()
    }

    private fun initUI() {
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        binding.bottomNavView.setupWithNavController(navController)
        binding.drawerNavView.setupWithNavController(navController)
        setSupportActionBar(binding.toolbar)

        mainViewModel.placesAutocompleteLiveData.observe(this, { restaurantsPlaceSearch ->
            val restaurantPlaceName = mutableListOf<String>()
            for (restaurant in restaurantsPlaceSearch) {
                if (restaurant.types.contains("restaurant"))
                    restaurantPlaceName.add(restaurant.structured_formatting.main_text)
            }

            binding.searchAutoCompleteTextView.setAdapter(
                ArrayAdapter(
                    this, android.R.layout.simple_dropdown_item_1line,
                    restaurantPlaceName
                )
            )
        })

        binding.toolbarOpenSearchButton.setOnClickListener {
            binding.toolbarSearchContainer.slideRight()
            binding.toolbarOpenSearchButton.visibility = View.GONE
        }

        binding.searchAutoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {}
            override fun beforeTextChanged(text: CharSequence?,start: Int,count: Int,after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                if (text?.isEmpty() == true){
                    binding.searchAutoCompleteTextView.setAdapter(null)

                }
                    mainViewModel.setInput(binding.searchAutoCompleteTextView.text.toString())
                    mainViewModel.getPlaces(binding.searchAutoCompleteTextView.text.toString())
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
            binding.searchAutoCompleteTextView.setText("")
            mainViewModel.searchInput.value = ""
            mainViewModel.placesAutocompleteLiveData.value = listOf()
            updateUiAfterSearch()
            binding.toolbarSearchContainer.slideLeft()
            binding.toolbarOpenSearchButton.visibility = View.VISIBLE
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
                .resize(60, 60)
                .into(headerBinding.drawerHeaderUserImage)
        })
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
                            null,
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

}
