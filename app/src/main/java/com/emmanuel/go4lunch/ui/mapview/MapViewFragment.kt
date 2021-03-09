package com.emmanuel.go4lunch.ui.mapview

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.di.Injection
import com.emmanuel.go4lunch.utils.REQUEST_PERMISSIONS_CODE_FINE_LOCATION
import com.emmanuel.go4lunch.utils.isSameDay
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


class MapViewFragment : Fragment(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private lateinit var supportFragmentManager: SupportMapFragment
    private var lastKnownLocation: Location? = null
    private lateinit var locationFab: FloatingActionButton
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mRestaurantsDetails: List<NearByRestaurant>
    private lateinit var mWorkmates: List<Workmate>
    private val factory = Injection.provideViewModelFactory()
    private lateinit var mapViewViewModel: MapViewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_map_view, container, false)
        mapViewViewModel = ViewModelProvider(this, factory).get(MapViewViewModel::class.java)
        supportFragmentManager =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        locationFab = rootView.findViewById(R.id.fragment_map_view_gps_fab)
        locationFab.setOnClickListener {
            registerLocationUpdate()
        }
        supportFragmentManager.getMapAsync(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mapViewViewModel.workmatesLiveData.observe(viewLifecycleOwner, { workmates ->
            mWorkmates = workmates
            addMarker()
        })
        mapViewViewModel.restaurantsDetailLiveData.observe(viewLifecycleOwner, { restaurants ->
           mRestaurantsDetails = restaurants
            addMarker()
        })
        mapViewViewModel.getAllWorkmate()
        return rootView
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
            mMap!!.setOnMarkerClickListener { marker ->
                val action = MapViewFragmentDirections.actionMapViewFragmentToRestaurantDetail(
                    null, mRestaurantsDetails[marker.title.toInt()].placeId
                )
                findNavController().navigate(action)
                true
            }
            registerLocationUpdate()
            updateLocationUI()
        }
    }

    private fun updateLocationUI() {
        mMap?.let {
            try {
                if (isPermissionLocationGranted()) {
                    mMap!!.isMyLocationEnabled = true
                    mMap!!.uiSettings?.isMyLocationButtonEnabled = false
                    locationFab.setImageResource(R.drawable.ic_baseline_gps_fixed_24)
                } else {
                    mMap!!.isMyLocationEnabled = false
                    mMap!!.uiSettings?.isMyLocationButtonEnabled = false
                    locationFab.setImageResource(R.drawable.ic_baseline_gps_off_24)
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }
    }

    /**
     * Register for location updates from [mFusedLocationClient], through callback on the main looper.
     * Once the location is available the device will be able to launch the query for get restaurant
     * detail near the current place through [mapViewViewModel]
     */
    private fun registerLocationUpdate() {
        if (isPermissionLocationGranted()) {
            try {
                locationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 3000
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        locationResult ?: return
                        if (locationResult.locations.isNotEmpty()) {
                            lastKnownLocation = locationResult.lastLocation
                            mMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM
                                )
                            )
                            mapViewViewModel.getAllNearRestaurant(lastKnownLocation)
                            mFusedLocationClient.removeLocationUpdates(locationCallback)
                        }
                    }
                }
                mFusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_CODE_FINE_LOCATION
            )
        }
        updateLocationUI()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_CODE_FINE_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog()
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerLocationUpdate()
            }
        }
        updateLocationUI()
    }

    private fun addMarker() {
        if (this::mRestaurantsDetails.isInitialized  && this::mWorkmates.isInitialized) {

            for (i in 0 until mRestaurantsDetails.count()) {
                var icon: BitmapDescriptor =
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_map_restaurant)
                for (workmate in mWorkmates) {
                    workmate.restaurantFavorite?.let {
                        if (workmate.restaurantFavorite.equals(mRestaurantsDetails[i].placeId) && isSameDay(
                                workmate.favoriteDate,
                                Calendar.getInstance().time
                            )
                        )
                            icon =
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_map_restaurant_favorite)
                    }
                }
                mMap?.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                mRestaurantsDetails[i].geometry.location.lat,
                                mRestaurantsDetails[i].geometry.location.lng
                            )
                        )
                        .title("$i")
                        .icon(icon)
                        .flat(true)
                )
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

    companion object {
        private const val DEFAULT_ZOOM = 15.0f
    }

    override fun onStop() {
        super.onStop()
        if (!::locationCallback.isInitialized && !::mFusedLocationClient.isInitialized) {
            mFusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}