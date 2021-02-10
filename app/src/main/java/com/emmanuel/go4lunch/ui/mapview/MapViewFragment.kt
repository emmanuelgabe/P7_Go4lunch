package com.emmanuel.go4lunch.ui.mapview

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.emmanuel.go4lunch.BuildConfig
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.RetrofitBuilder
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.utils.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*


class MapViewFragment : Fragment(), OnMapReadyCallback {
    private var locationPermissionGranted: Boolean = false
    private lateinit var mMap: GoogleMap
    private lateinit var supportFragmentManager: SupportMapFragment
    private var lastKnownLocation: Location? = null
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_map_view, container, false)
        supportFragmentManager =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        // Async map
        supportFragmentManager.getMapAsync(this)

        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity());
        return rootView
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
            mMap.setOnMyLocationButtonClickListener(GoogleMap.OnMyLocationButtonClickListener {
                // TODO check gps statut and active thme if nessesary

                fetchNearRestaurantLocation()
                false
            })
            mMap.setOnMarkerClickListener { marker ->
                Toast.makeText(requireContext(), marker.title, Toast.LENGTH_LONG)
                true
            }
            // Turn on the My Location layer and the related control on the map.
            updateLocationUI()
            // Get the current location of the device and set the position of the map.
            getDeviceLocation()
        }
    }

    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM
                                )
                            )
                        }
                    } else {
                        Log.d(Companion.TAG, "Current location is null. Using defaults.")
                        Log.e(Companion.TAG, "Exception: %s", task.exception)
                        // TODO add popup information if gps is disabled
                        mMap.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onResume() {
        super.onResume()
        getLocationPermission()
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    companion object {
        private const val TAG = "MapViewFragment"
        private const val DEFAULT_ZOOM = 15.0f
    }

    private fun fetchNearRestaurantLocation() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitBuilder.googleMapsService.getNearRestaurantId(
                "${lastKnownLocation?.latitude},${lastKnownLocation?.longitude}",               500,
                "restaurant",
                BuildConfig.GOOGLE_MAP_API_KEY
            )
            withContext(Dispatchers.Main) {
                var list = mutableListOf<Restaurant>()
                if (response.isSuccessful) {
                    val items = response.body()?.results
                    if (items != null) {
                        for (i in 0 until items.count()) {
                            addMarker(
                                items[i].placeId,
                                items[i].geometry.location.lat,
                                items[i].geometry.location.lng
                            )
                        }
                    }
                } else {
                    Log.e("RETROFIT_ERROR", response.code().toString())
                }
            }
        }
    }

    fun addMarker(id: String, lat: Double, lng: Double) {

        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .title(id)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_restaurant))
                .flat(true)
        )
    }
}