package com.emmanuel.go4lunch.ui.listview

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.MainViewModel
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.response.Prediction
import com.emmanuel.go4lunch.data.database.model.RestaurantDetail
import com.emmanuel.go4lunch.databinding.FragmentListViewBinding
import com.emmanuel.go4lunch.ui.settings.SettingsFragment
import com.emmanuel.go4lunch.utils.FetchLocationEvent
import com.emmanuel.go4lunch.utils.REQUEST_PERMISSIONS_CODE_FINE_LOCATION
import org.greenrobot.eventbus.EventBus

class ListViewFragment : Fragment() {
    private lateinit var binding: FragmentListViewBinding
    private lateinit var mAdapter: ListViewAdapter
    private val mainViewModel: MainViewModel by activityViewModels()

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
        initObserver()
        updateRestaurantList()
        registerLocationUpdate()
        return rootView
    }

    private fun registerLocationUpdate() {
        if (isPermissionLocationGranted()) {
            EventBus.getDefault().post(FetchLocationEvent())
            if (!binding.fragmentListViewSwipeContainer.isVisible) {
                binding.fragmentListViewProgressBar.visibility = View.VISIBLE
                binding.fragmentListViewMessageInformation.text =
                    getString(R.string.fragment_list_view_message_search_position)
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
            mainViewModel.lastKnownLocation.value == null -> {
                binding.fragmentListViewMessageInformation.text =
                    getString(R.string.fragment_list_view_message_localization)
            }
            mainViewModel.restaurantsDetailLiveData.value == null && mainViewModel.workmatesLiveData.value != null -> {
                binding.fragmentListViewMessageInformation.text =
                    getString(R.string.fragment_list_view_message_download_restaurant)
            }
            mainViewModel.restaurantsDetailLiveData.value != null  && mainViewModel.workmatesLiveData.value == null -> {
                binding.fragmentListViewMessageInformation.text =
                getString(R.string.fragment_list_view_message_search_workmates)
            }
            mainViewModel.restaurantsDetailLiveData.value == null && mainViewModel.workmatesLiveData.value != null -> {
                binding.fragmentListViewMessageInformation.text =
                    getString(R.string.fragment_list_view_message_download)
            }
            mainViewModel.restaurantsDetailLiveData.value != null && mainViewModel.workmatesLiveData.value != null  -> {
                if ( mainViewModel.restaurantsDetailLiveData.value!!.isNotEmpty()) {
                    // Display all near restaurants if no research has been done or search field is empty
                    if (restaurantsPlacesSearch == null || restaurantsPlacesSearch.isEmpty()) {
                        mAdapter.updateRestaurantsList(
                            mainViewModel.restaurantsDetailLiveData.value!!,
                            mainViewModel.lastKnownLocation.value,
                            mainViewModel.workmatesLiveData.value!!
                        )
                    } else {  // Display near restaurants corresponding to the search
                        val restaurantPlaceIdSearch = mutableListOf<String>()
                        for (restaurant in restaurantsPlacesSearch) {
                            if (restaurant.types.contains("restaurant"))
                                restaurantPlaceIdSearch.add(restaurant.place_id)
                        }
                        val restaurantDetailSearchList = mutableListOf<RestaurantDetail>()
                        for (restaurantDetail in mainViewModel.restaurantsDetailLiveData.value!!) {
                            if (restaurantPlaceIdSearch.contains(restaurantDetail.id))
                                restaurantDetailSearchList.add(restaurantDetail)
                        }
                        mAdapter.updateRestaurantsList(
                            restaurantDetailSearchList,
                            mainViewModel.lastKnownLocation.value,
                            mainViewModel.workmatesLiveData.value!!
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

    private fun initObserver() {
        mainViewModel.workmatesLiveData.observe(viewLifecycleOwner, {
            updateRestaurantList()
        })
        mainViewModel.restaurantsDetailLiveData.observe(viewLifecycleOwner, {
            updateRestaurantList()
        })
        mainViewModel.placesAutocompleteLiveData.observe(viewLifecycleOwner,
            { restaurantsPlaceSearch ->
                updateRestaurantList(restaurantsPlaceSearch)
            })
        mainViewModel.lastKnownLocation.observe(viewLifecycleOwner,
            { lastKnownLocation ->
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val radiusPreference = sharedPreferences.getInt(SettingsFragment.KEY_PREF_RESTAURANT_RADIUS,1000)
                mainViewModel.getAllNearRestaurant(radiusPreference)
                })
        mainViewModel.nearRestaurantLiveData.observe(viewLifecycleOwner,
            { nearRestaurant ->
                mainViewModel.getAllDetailRestaurant()
            })
    }
}