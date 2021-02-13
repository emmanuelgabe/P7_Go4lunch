package com.emmanuel.go4lunch.ui.listview

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.databinding.FragmentListViewBinding
import com.emmanuel.go4lunch.utils.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class ListViewFragment : Fragment() {
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted: Boolean = false
    private var lastKnownLocation: Location? = null
    private val TAG = "ListViewFragment"
    private lateinit var binding: FragmentListViewBinding
    private lateinit var mAdapter: ListViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootvView: View = inflater.inflate(R.layout.fragment_list_view, container, false)

        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        rootvView.setOnClickListener {
            Toast.makeText(requireContext(), "test", Toast.LENGTH_LONG).show()
        }
        binding = FragmentListViewBinding.bind(rootvView)
        binding.fragmentListViewSwipeContainer.setOnRefreshListener {
            fetchNearRestaurantDetail()
        }
        binding = FragmentListViewBinding.bind(rootvView)
        binding.listViewRecyclerView.layoutManager = LinearLayoutManager(activity)
        fetchNearRestaurantDetail()
        return rootvView
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                    } else {
                        // TODO add popup information if gps is disabled
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
        getDeviceLocation()
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
        fetchNearRestaurantDetail()
    }

    private fun fetchNearRestaurantDetail() {
        CoroutineScope(IO).launch {
            val executionTime = measureTimeMillis {
                val nearRestaurants = async {
                    Log.d(TAG, "launching gatAllNearRestaurant  ${Thread.currentThread().name}")
                    RestaurantRepository.getAllNearRestaurant(lastKnownLocation)
                }.await()
                Log.d(TAG, "finish getAllNearRestaurant")
                val restaurantsDetail = async {
                    Log.d(TAG, "launching getAllDetailRestaurant()  ${Thread.currentThread().name}")
                    RestaurantRepository.getAllDetailRestaurant(nearRestaurants)
                }.await()
                Log.d(TAG, "finish getAllDetailRestaurant")
                val updateUiJob = launch {
                    Log.d(TAG, "updating restaurant list in ui  ${Thread.currentThread().name}")
                    updateRestaurantList(restaurantsDetail)
                }
            }
            Log.d(TAG, "fetchNearRestaurantDetail and update ui in : ${executionTime}ms")
        }
    }

    private suspend fun updateRestaurantList(restaurantsDetail: MutableList<NearByRestaurant>) {
        withContext(Main) {
            if (!::mAdapter.isInitialized) {
                mAdapter = ListViewAdapter(restaurantsDetail)
                binding.listViewRecyclerView.adapter = mAdapter
            }
            mAdapter.updateRestaurantsList(restaurantsDetail, lastKnownLocation)
            binding.fragmentListViewSwipeContainer.isRefreshing = false
        }
    }
}