package com.emmanuel.go4lunch.ui.mapview

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.emmanuel.go4lunch.MainViewModel
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.ui.settings.SettingsFragment
import com.emmanuel.go4lunch.utils.FetchLocationEvent
import com.emmanuel.go4lunch.utils.hideKeyboard
import com.emmanuel.go4lunch.utils.isSameDay
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.greenrobot.eventbus.EventBus
import java.util.*

class MapViewFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var supportFragmentManager: SupportMapFragment
    private lateinit var locationFab: FloatingActionButton
    private var isFirstZoom = true
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_map_view, container, false)
        supportFragmentManager =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        locationFab = rootView.findViewById(R.id.fragment_map_view_gps_fab)
        locationFab.setOnClickListener {
            registerLocationUpdate()
        }
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {
                    registerLocationUpdate()
                } else {
                    showPermissionDialog()
                }
                updateLocationUI()
            }
        supportFragmentManager.getMapAsync(this)
        initObserver()
        return rootView
    }

    private fun registerLocationUpdate() {
        when{
            isPermissionLocationGranted() -> {
                EventBus.getDefault().post(FetchLocationEvent())
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showPermissionDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        updateLocationUI()
    }

    private fun initObserver() {
        mainViewModel.workmatesLiveData.observe(viewLifecycleOwner, {
            addMarker()
        })
        mainViewModel.nearRestaurantsLiveData.observe(viewLifecycleOwner, {
            addMarker()
        })
        mainViewModel.placesAutocompleteLiveData.observe(viewLifecycleOwner,
            { restaurantsPlaceSearch ->
                if (restaurantsPlaceSearch.isEmpty()) {
                    addMarker()
                } else {
                    val restaurantPlaceId = mutableListOf<String>()
                    for (restaurant in restaurantsPlaceSearch) {
                        if (restaurant.types.contains("restaurant"))
                            restaurantPlaceId.add(restaurant.place_id)
                    }
                    addMarker(restaurantPlaceId)
                }
            })
        mainViewModel.lastKnownLocation.observe(viewLifecycleOwner, { lastKnownLocation ->
            if (::mMap.isInitialized) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val mapZoomPreference =
                    sharedPreferences.getInt(SettingsFragment.KEY_PREF_MAP_ZOOM, 15)
                val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        lastKnownLocation.latitude,
                        lastKnownLocation.longitude
                    ), mapZoomPreference.toFloat()
                )
                if (isFirstZoom) {
                    mMap.moveCamera(
                        cameraUpdateFactory
                    )
                    isFirstZoom = false
                } else {
                    mMap.animateCamera(
                        cameraUpdateFactory
                    )
                }
            }
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val radiusPreference =
                sharedPreferences.getInt(SettingsFragment.KEY_PREF_RESTAURANT_RADIUS, 1000)
            mainViewModel.getAllNearRestaurant(radiusPreference)
        })
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
            mMap.setOnMarkerClickListener { marker ->
                val action = MapViewFragmentDirections.actionMapViewFragmentToRestaurantDetail(
                    marker.title
                )
                findNavController().navigate(action)
                true
            }
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val mapZoomPreference = sharedPreferences.getInt(SettingsFragment.KEY_PREF_MAP_ZOOM, 15)
            if (mainViewModel.lastKnownLocation.value == null) {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), mapZoomPreference.toFloat())
                )
            } else {
                val location = mainViewModel.lastKnownLocation.value
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location!!.latitude,
                            location.longitude,
                        ), mapZoomPreference.toFloat()
                    )
                )
            }
            addMarker()
            registerLocationUpdate()
            updateLocationUI()
        }
    }

    private fun updateLocationUI() {
        mMap.let {
            try {
                if (isPermissionLocationGranted()) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings?.isMyLocationButtonEnabled = false
                    locationFab.setImageResource(R.drawable.ic_baseline_gps_fixed_24)
                } else {
                    mMap.isMyLocationEnabled = false
                    mMap.uiSettings?.isMyLocationButtonEnabled = false
                    locationFab.setImageResource(R.drawable.ic_baseline_gps_off_24)
                }
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        context,
                        R.raw.map_style
                    )
                )
                mMap.setOnMapClickListener {
                    hideKeyboard()
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }
    }

    private fun addMarker(placesSearchId: List<String>? = null) {
        if (mainViewModel.nearRestaurantsLiveData.value != null
            && mainViewModel.workmatesLiveData.value != null
            && ::mMap.isInitialized
        ) {
            mMap.clear()
            for (restaurant in mainViewModel.nearRestaurantsLiveData.value!!) {
                if (placesSearchId == null || placesSearchId.contains(restaurant.placeId)) {
                    var icon: BitmapDescriptor =
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_map_restaurant)
                    for (workmate in mainViewModel.workmatesLiveData.value!!) {
                        workmate.restaurantFavorite?.let {
                            if (workmate.restaurantFavorite.equals(restaurant.placeId) && isSameDay(
                                    workmate.favoriteDate,
                                    Calendar.getInstance().time))
                                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_restaurant_favorite)
                        }
                    }
                    mMap.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    restaurant.geometry.location.lat,
                                    restaurant.geometry.location.lng
                                )
                            )
                            .title(restaurant.placeId)
                            .icon(icon)
                            .flat(true)
                    )
                }
            }
        }
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage(getString(R.string.alert_dialog_permission_location_message))
            setTitle(getString(R.string.alert_dialog_permission_title))
            setPositiveButton(getString(R.string.alert_dialog_permission_button_setting)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            setNegativeButton(getString(R.string.alert_dialog_permission_button_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun isPermissionLocationGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}