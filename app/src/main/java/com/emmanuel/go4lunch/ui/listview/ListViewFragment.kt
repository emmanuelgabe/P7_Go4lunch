package com.emmanuel.go4lunch.ui.listview

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.databinding.FragmentListViewBinding
import com.emmanuel.go4lunch.utils.REQUEST_PERMISSIONS_CODE_FINE_LOCATION
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class ListViewFragment : Fragment() {
    private var lastKnownLocation: Location? = null
    private lateinit var binding: FragmentListViewBinding
    private lateinit var mAdapter: ListViewAdapter
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_list_view, container, false)
        binding = FragmentListViewBinding.bind(rootView)
        binding.fragmentListViewSwipeContainer.setOnRefreshListener {
            registerLocationUpdate()
        }
        binding = FragmentListViewBinding.bind(rootView)
        binding.listViewRecyclerView.layoutManager = LinearLayoutManager(activity)
        mAdapter = ListViewAdapter()
        binding.listViewRecyclerView.adapter = mAdapter
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        registerLocationUpdate()
        return rootView
    }
    /**
     * Register for location updates from [mFusedLocationClient], through callback on the main looper.
     * Once the location is available the device will be able to launch the requests with [fetchNearRestaurantDetail]
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
                            fetchNearRestaurantDetail()
                            mFusedLocationClient.removeLocationUpdates(locationCallback)
                        }
                    }
                }
                mFusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.getMainLooper()
                )
                if (!binding.fragmentListViewSwipeContainer.isVisible) {
                    binding.fragmentListViewProgressBar.visibility = View.VISIBLE
                    binding.fragmentListViewMessageInformation.text = getString(R.string.fragment_list_view_message_search_position)
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_CODE_FINE_LOCATION
            )
            binding.fragmentListViewSwipeContainer.isRefreshing = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_CODE_FINE_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                binding.fragmentListViewMessageInformation.text = getString(R.string.fragment_list_view_message_no_permission)
                showPermissionDialog()
            }else if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                registerLocationUpdate()
            }
        }
    }

    private fun fetchNearRestaurantDetail() {
        CoroutineScope(IO).launch {
            val executionTime = measureTimeMillis {
                withContext(Main) {
                    binding.fragmentListViewMessageInformation.text =
                        getString(R.string.fragment_list_view_message_download)
                }
                val restaurantsDetailList = RestaurantRepository.getAllDetailRestaurant(lastKnownLocation)
                val workmatesList = WorkmateRepository.getAllWorkmate()
                launch {
                    updateRestaurantList(restaurantsDetailList,workmatesList)
                }.join()
            }
            Log.d(TAG, "fetchNearRestaurantDetail and update ui in : ${executionTime}ms")
        }
    }

    private suspend fun updateRestaurantList(
        restaurantsDetailList: List<NearByRestaurant>,
        workmatesList: List<Workmate>
    ) {
        withContext(Main) {
            if (restaurantsDetailList.isNotEmpty()) {
                mAdapter.updateRestaurantsList(restaurantsDetailList, lastKnownLocation,workmatesList)
                binding.fragmentListViewMessageInformation.visibility = View.GONE
                binding.fragmentListViewProgressBar.visibility = View.GONE
                binding.fragmentListViewMessageInformation.text = ""
                binding.fragmentListViewSwipeContainer.visibility = View.VISIBLE
            } else {
                binding.fragmentListViewMessageInformation.text =
                    getString(R.string.fragment_list_view_message_no_restaurant_found)
            }
            binding.fragmentListViewSwipeContainer.isRefreshing = false
        }
    }
    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage(getString(R.string.alert_dialog_permission_message))
            setTitle(getString(R.string.alert_dialog_permission_title))
            setPositiveButton(getString(R.string.alert_dialog_permission_button_setting)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            setNegativeButton(getString(R.string.alert_dialog_permission_button_cancel)){ dialog, _ ->
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
        private const val TAG = "ListViewFragment"
    }
    override fun onStop() {
        super.onStop()
        if (!::locationCallback.isInitialized && !::mFusedLocationClient.isInitialized) {
            mFusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}