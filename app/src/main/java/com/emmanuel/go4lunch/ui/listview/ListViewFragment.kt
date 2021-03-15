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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.MainViewModel
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.api.response.Prediction
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.FragmentListViewBinding
import com.emmanuel.go4lunch.di.Injection
import com.emmanuel.go4lunch.utils.REQUEST_PERMISSIONS_CODE_FINE_LOCATION
import com.google.android.gms.location.*

class ListViewFragment : Fragment() {
    private var lastKnownLocation: Location? = null
    private lateinit var binding: FragmentListViewBinding
    private lateinit var mAdapter: ListViewAdapter
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var factory = Injection.provideViewModelFactory()
    private lateinit var listViewModel: ListViewViewModel
    private lateinit var mRestaurantsDetailList: List<NearByRestaurant>
    private lateinit var mWorkmatesList: List<Workmate>
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listViewModel = ViewModelProvider(this, factory).get(ListViewViewModel::class.java)
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
        initObserver()
        updateRestaurantList()
        registerLocationUpdate()
        return rootView
    }
    /**
     * Register for location updates from [mFusedLocationClient], through callback on the main looper.
     * Once the location is available the device will be able to launch the query for get restaurant
     * detail near the current place through [listViewModel]
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
                            mainViewModel.saveLocation(lastKnownLocation)
                            listViewModel.getAllDetailRestaurant(lastKnownLocation)
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
                    binding.fragmentListViewMessageInformation.text =
                        getString(R.string.fragment_list_view_message_search_position)
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
                binding.fragmentListViewMessageInformation.text =
                    getString(R.string.fragment_list_view_message_no_permission)
                showPermissionDialog()
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerLocationUpdate()
            }
        }
    }

    private fun updateRestaurantList(restaurantsPlacesSearch: List<Prediction>? = null) {
        when {
            this::mRestaurantsDetailList.isInitialized && this::mWorkmatesList.isInitialized -> {
                if (mRestaurantsDetailList.isNotEmpty()) {
                    // Display all near restaurants if no research has been done or search field is empty
                    if (restaurantsPlacesSearch == null || restaurantsPlacesSearch.isEmpty()) {
                        mAdapter.updateRestaurantsList(
                            mRestaurantsDetailList,
                            lastKnownLocation,
                            mWorkmatesList
                        )
                    } else {  // Display near restaurants corresponding to the search
                        val restaurantPlaceIdSearch = mutableListOf<String>()
                        for (restaurant in restaurantsPlacesSearch) {
                            if (restaurant.types.contains("restaurant"))
                                restaurantPlaceIdSearch.add(restaurant.place_id)
                        }
                        val restaurantDetailSearchList = mutableListOf<NearByRestaurant>()
                        for (restaurantDetail in mRestaurantsDetailList) {
                            if (restaurantPlaceIdSearch.contains(restaurantDetail.placeId))
                                restaurantDetailSearchList.add(restaurantDetail)
                        }
                        mAdapter.updateRestaurantsList(
                            restaurantDetailSearchList,
                            lastKnownLocation,
                            mWorkmatesList
                        )
                    }
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
            !this::mRestaurantsDetailList.isInitialized && this::mWorkmatesList.isInitialized -> {
                binding.fragmentListViewMessageInformation.text =
                    getString(R.string.fragment_list_view_message_download_restaurant)
            }
            this::mRestaurantsDetailList.isInitialized && !this::mWorkmatesList.isInitialized -> {
                binding.fragmentListViewMessageInformation.text =
                getString(R.string.fragment_list_view_message_search_workmates)
            }
            !(this::mRestaurantsDetailList.isInitialized && !this::mWorkmatesList.isInitialized) -> {
                binding.fragmentListViewMessageInformation.text =
                    getString(R.string.fragment_list_view_message_download)
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

    override fun onStop() {
        super.onStop()
        if (!::locationCallback.isInitialized && !::mFusedLocationClient.isInitialized) {
            mFusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    private fun initObserver() {
        mainViewModel.workmatesLiveData.observe(viewLifecycleOwner, { workmates ->
            mWorkmatesList = workmates
            updateRestaurantList()
        })
        listViewModel.restaurantsDetailLiveData.observe(
            viewLifecycleOwner,
            { restaurants ->
                mRestaurantsDetailList = restaurants
                updateRestaurantList()
            })
       mainViewModel.placesAutocompleteLiveData.observe(viewLifecycleOwner,
            { restaurantsPlaceSearch ->
                updateRestaurantList(restaurantsPlaceSearch)
            })
    }
}