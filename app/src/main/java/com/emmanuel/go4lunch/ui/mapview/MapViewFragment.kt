package com.emmanuel.go4lunch.ui.mapview

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.utils.REQUEST_PERMISSIONS_CODE_FINE_LOCATION
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapViewFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var supportFragmentManager: SupportMapFragment
    private var lastKnownLocation: Location? = null
    lateinit var locationFab: FloatingActionButton
    private val TAG = "MapViewFragment"
    private val DEFAULT_ZOOM = 15.0f
    private val restaurants = mutableListOf<NearByRestaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_map_view, container, false)
        supportFragmentManager =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        locationFab = rootView.findViewById(R.id.fragment_map_view_gps_fab)
        locationFab.setOnClickListener {
            getDeviceLocation()
            checkLocationPermission()
        }
        // Async map
        supportFragmentManager.getMapAsync(this)
        getDeviceLocation()
        return rootView
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
            mMap.setOnMarkerClickListener { marker ->
                val action = MapViewFragmentDirections.actionMapViewFragmentToRestaurantDetail(
                    restaurants.get(marker.title.toInt()))
                findNavController().navigate(action)
                true
            }
            updateLocationUI()
        }
    }

    private fun updateLocationUI() {
         try {
             if (isPermissionLocationGranted()) {
                 mMap.isMyLocationEnabled = true
                 mMap.uiSettings?.isMyLocationButtonEnabled = false
                 locationFab.setImageResource(R.drawable.ic_baseline_gps_fixed_24)
             }else{
                 mMap.isMyLocationEnabled = false
                 mMap.uiSettings?.isMyLocationButtonEnabled = false
                 locationFab.setImageResource(R.drawable.ic_baseline_gps_off_24)
             }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDeviceLocation() {
        try {
            if (isPermissionLocationGranted()) {
                val locationResult = LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation
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
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun checkLocationPermission() {
        if(isPermissionLocationGranted()) {
                fetchNearRestaurantLocation()
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
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog()
            }
        }
        updateLocationUI()
    }


    private fun fetchNearRestaurantLocation() {
        CoroutineScope(IO).launch {
            RestaurantRepository.getAllNearRestaurant(lastKnownLocation)?.let {
                restaurants.clear()
                restaurants.addAll(it)
            }
            restaurants.let { addMarker(restaurants) }
        }
    }

    suspend fun addMarker(items: List<NearByRestaurant>) {
        withContext(Main) {
            for (i in 0 until items.count()) {
                mMap.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                items[i].geometry.location.lat,
                                items[i].geometry.location.lng
                            )
                        )
                        .title("$i")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_restaurant))
                        .flat(true)
                )
            }
        }
    }
    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage("Permission to access your location is required to find restaurant in the vicinity. \nTo ensure the proper functioning of this functionality the location permission must be activated in the application settings")
            setTitle("Permission required")
            setPositiveButton("Setting") { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.setData(uri)
                startActivity(intent)
            }
            setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
    fun isPermissionLocationGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}