package com.emmanuel.go4lunch.ui.listview

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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.databinding.FragmentListViewBinding
import com.emmanuel.go4lunch.utils.REQUEST_PERMISSIONS_CODE_FINE_LOCATION
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class ListViewFragment : Fragment() {
    private var lastKnownLocation: Location? = null
    private val TAG = "ListViewFragment"
    private lateinit var binding: FragmentListViewBinding
    private lateinit var mAdapter: ListViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_list_view, container, false)
        binding = FragmentListViewBinding.bind(rootView)
        binding.fragmentListViewSwipeContainer.setOnRefreshListener {
            getDeviceLocation()
            checkLocationPermission()
        }
        binding = FragmentListViewBinding.bind(rootView)
        binding.listViewRecyclerView.layoutManager = LinearLayoutManager(activity)
        mAdapter = ListViewAdapter()
        binding.listViewRecyclerView.adapter = mAdapter
        getDeviceLocation()
        return rootView
    }

    private fun getDeviceLocation() {
        try {
            if (isPermissionLocationGranted()) {
                val locationResult = LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            lastKnownLocation = task.result
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun checkLocationPermission() {
        if (isPermissionLocationGranted()) {
            if (lastKnownLocation != null) {
                fetchNearRestaurantDetail()
            } else {
                binding.fragmentListViewMessageInformation.text =
                    getString(R.string.fragment_list_view_message_no_location)
                binding.fragmentListViewSwipeContainer.isRefreshing = false
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
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog()
            }
        }
    }

    private fun fetchNearRestaurantDetail() {
        CoroutineScope(IO).launch {
            val executionTime = measureTimeMillis {
                val nearRestaurants = async {
                    RestaurantRepository.getAllNearRestaurant(lastKnownLocation)
                }.await()
                val restaurantsDetailList = async {
                    RestaurantRepository.getAllDetailRestaurant(nearRestaurants)
                }.await()
                launch {
                    updateRestaurantList(restaurantsDetailList)
                }.join()
            }
            Log.d(TAG, "fetchNearRestaurantDetail and update ui in : ${executionTime}ms")
        }
    }

    private suspend fun updateRestaurantList(restaurantsDetailList: List<NearByRestaurant>) {
        withContext(Main) {
            if (restaurantsDetailList.size > 0) {
                mAdapter.updateRestaurantsList(restaurantsDetailList, lastKnownLocation)
                binding.fragmentListViewMessageInformation.visibility = View.GONE
            }
            else{
                val strMessage = "${getString(R.string.fragment_list_view_message_no_restaurant_found)} \n ${getString(
                    R.string.fragment_list_view_message_information)}"
                binding.fragmentListViewMessageInformation.text = strMessage
            }
            binding.fragmentListViewSwipeContainer.isRefreshing = false
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