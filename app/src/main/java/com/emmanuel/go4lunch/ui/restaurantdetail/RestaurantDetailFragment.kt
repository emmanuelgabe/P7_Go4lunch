package com.emmanuel.go4lunch.ui.restaurantdetail

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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.emmanuel.go4lunch.App
import com.emmanuel.go4lunch.MainViewModel
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.database.model.RestaurantDetailEntity
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.FragmentRestaurantDetailBinding
import com.emmanuel.go4lunch.di.ViewModelFactory
import com.emmanuel.go4lunch.notification.NotificationWorker
import com.emmanuel.go4lunch.ui.settings.SettingsFragment
import com.emmanuel.go4lunch.utils.MAX_WITH_IMAGE
import com.emmanuel.go4lunch.utils.ResetSearchView
import com.emmanuel.go4lunch.utils.getPhotoUrlFromReference
import com.emmanuel.go4lunch.utils.isSameDay
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

class RestaurantDetailFragment : Fragment() {
    @Inject lateinit var factory: ViewModelFactory
    private lateinit var binding: FragmentRestaurantDetailBinding
    private lateinit var mAdapter: RestaurantDetailAdapter
    private lateinit var restaurantDetailViewModel: RestaurantDetailViewModel
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.app().appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        restaurantDetailViewModel =
            ViewModelProvider(this, factory).get(RestaurantDetailViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false)
        EventBus.getDefault().post(ResetSearchView())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        binding = FragmentRestaurantDetailBinding.bind(view)
        mAdapter = RestaurantDetailAdapter()
        restaurantDetailViewModel.init(arguments?.getString("restaurantId")!!)
        initObserver()
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {
                    callRestaurant()
                } else {
                    showPermissionDialog()
                }
            }
        return view
    }

    private fun initObserver() {
        mainViewModel.workmatesLiveData.observe(viewLifecycleOwner, {
            updateUi()
        })
        mainViewModel.textSearchInput.observe(viewLifecycleOwner, { workmateSearch ->
            if (restaurantDetailViewModel.currentRestaurantsDetailLiveData.value != null && mainViewModel.workmatesLiveData.value != null) {
                if (workmateSearch.isNotBlank()) {
                    val workmateSearchList = mutableListOf<Workmate>()
                    for (workmate in mainViewModel.workmatesLiveData.value!!) {
                        if (workmate.name!!.contains(workmateSearch, true)) {
                            workmateSearchList.add(workmate)
                        }
                    }
                    updateWorkmateList(workmateSearchList)
                } else {
                    updateWorkmateList()
                }
            }
        })
        restaurantDetailViewModel.currentRestaurantsDetailLiveData.observe(
            viewLifecycleOwner,
            { restaurantDetail ->
                updateUi()
                initUi(restaurantDetail)
            })
    }

    private fun initUi(currentRestaurant: RestaurantDetailEntity) {
        binding.restaurant = currentRestaurant
        if (currentRestaurant.rating != null) {
            val rating = currentRestaurant.rating.toFloat() * 3 / 5
            binding.fragmentRestaurantDetailRatingBar.rating = rating.roundToInt().toFloat()
        } else {
            binding.fragmentRestaurantDetailRatingBar.visibility = View.GONE
        }
        if (currentRestaurant.photoReference != null) {
            Picasso.get()
                .load(
                    getPhotoUrlFromReference(
                        currentRestaurant.photoReference,
                        MAX_WITH_IMAGE
                    )
                )
                .into(binding.activityDetailRestaurantImage)
        } else {
            binding.activityDetailRestaurantImage.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.activityDetailRestaurantImage.setImageResource(R.drawable.ic_no_photography_24)
        }
        binding.fragmentDetailButtonCall.setOnClickListener {
            if (currentRestaurant.phoneNumber != null) {
                if (currentRestaurant.phoneNumber.isNotBlank()) {
                    when{
                        ContextCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.CALL_PHONE
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            callRestaurant()
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) -> {
                            showPermissionDialog()
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        }
                    }
                }
            } else {
                Toast.makeText(
                    binding.root.context,
                    getString(R.string.restaurant_detail_fragment_message_no_number_found),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.fragmentRestaurantDetailRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.fragmentRestaurantDetailRecyclerView.adapter = mAdapter

        binding.detailFragmentButtonWebsite.setOnClickListener {
            if (!currentRestaurant.website.isNullOrBlank()) {
                val intent =
                    Intent(Intent.ACTION_VIEW).setData(Uri.parse(currentRestaurant.website))
                startActivity(intent)
            } else {
                Toast.makeText(
                    binding.root.context,
                    getString(R.string.restaurant_detail_fragment_message_no_web_site_found),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.fragmentDetailButtonLike.setOnClickListener {
            restaurantDetailViewModel.updateLikeRestaurant(mainViewModel.currentUserLiveData.value!!)
        }
        binding.fragmentDetailRestaurantFab.setOnClickListener {
            restaurantDetailViewModel.updateFavoriteRestaurant(
                mainViewModel.currentUserLiveData.value!!,
                mainViewModel.workmatesLiveData.value!!
            )
        }
    }

    private fun updateUi() {
        if (restaurantDetailViewModel.currentRestaurantsDetailLiveData.value != null && mainViewModel.workmatesLiveData.value != null) {
            // update like button
            if (!mainViewModel.currentUserLiveData.value?.restaurantsIdLike.isNullOrEmpty() && mainViewModel.currentUserLiveData.value!!.restaurantsIdLike!!.contains(
                    restaurantDetailViewModel.currentRestaurantsDetailLiveData.value!!.id
                )
            )
                binding.fragmentDetailButtonLike.setCompoundDrawablesWithIntrinsicBounds(
                    null, ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_baseline_thumb_up_alt_24
                    ), null, null
                )
            else
                binding.fragmentDetailButtonLike.setCompoundDrawablesWithIntrinsicBounds(
                    null, ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_baseline_thumb_up_off_alt_24
                    ), null, null
                )
            // update favorite button
            if (mainViewModel.currentUserLiveData.value!!.restaurantFavorite.equals(
                    restaurantDetailViewModel.currentRestaurantsDetailLiveData.value!!.id
                ) && isSameDay(
                    mainViewModel.currentUserLiveData.value!!.favoriteDate,
                    Calendar.getInstance().time
                )
            ) {
                binding.fragmentDetailRestaurantFab.setImageResource(R.drawable.ic_check_favorite_restaurant)
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val notificationPreference = sharedPreferences.getBoolean(
                    SettingsFragment.KEY_PREF_NOTIFICATION_PREFERENCE,
                    false
                )
                if (notificationPreference)
                    setUpAlarmManager()
            } else {
                binding.fragmentDetailRestaurantFab.setImageResource(R.drawable.ic_uncheck_favorite_restaurant)
                cancelAlarmManger()
            }
            updateWorkmateList()
        }
    }

    private fun callRestaurant() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse(
            "tel:${
                restaurantDetailViewModel.currentRestaurantsDetailLiveData.value?.phoneNumber!!.replace(
                    "\\s".toRegex(),
                    ""
                )
            }"
        )
        startActivity(intent)
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage(getString(R.string.alert_dialog_permission_call_phone_message))
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

    private fun updateWorkmateList(workmateSearchList: List<Workmate>? = null) {
        val workmates = mutableListOf<Workmate>()
        if (workmateSearchList == null) {
            workmates.addAll(mainViewModel.workmatesLiveData.value!!)
        } else {
            workmates.addAll(workmateSearchList)
        }
        val workmatesIsJoining = mutableListOf<Workmate>()
        for (workmate in workmates) {
            if (workmate.restaurantFavorite.equals(restaurantDetailViewModel.currentRestaurantsDetailLiveData.value!!.id) && isSameDay(
                    workmate.favoriteDate,
                    Calendar.getInstance().time
                )
            )
                workmatesIsJoining.add(workmate)
        }
        mAdapter.submitList(workmatesIsJoining.toList())
    }

    private fun setUpAlarmManager() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val notificationHour = sharedPreferences.getString(
            SettingsFragment.KEY_PREF_NOTIFICATION_HOUR_PREFERENCE,
            "12"
        )!!
            .toInt()

        val alarmTime = Calendar.getInstance()
        val currentTime = alarmTime.timeInMillis
        val currentHour = alarmTime.get(Calendar.HOUR_OF_DAY)
        if (currentHour >= notificationHour) {
            alarmTime.add(Calendar.DAY_OF_MONTH, 1)
        }
        alarmTime.set(Calendar.HOUR_OF_DAY, notificationHour)
        alarmTime.set(Calendar.MINUTE, 0)
        alarmTime.set(Calendar.SECOND, 0)
        alarmTime.set(Calendar.MILLISECOND, 0)

        val timeDiff = alarmTime.timeInMillis - currentTime

        val workerConstraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workerData = workDataOf(
            "userId" to mainViewModel.currentUserLiveData.value!!.uid,
            "restaurantId" to mainViewModel.currentUserLiveData.value!!.restaurantFavorite,
            "restaurantName" to restaurantDetailViewModel.currentRestaurantsDetailLiveData.value!!.name,
            "restaurantAddress" to restaurantDetailViewModel.currentRestaurantsDetailLiveData.value!!.address
        )

        val workRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(workerData)
            .setConstraints(workerConstraints)
            //.setInitialDelay(20, TimeUnit.SECONDS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(requireContext())
            .enqueueUniqueWork(NOTIFICATION_WORKER_NAME, ExistingWorkPolicy.REPLACE, workRequest)
    }

    private fun cancelAlarmManger() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork(NOTIFICATION_WORKER_NAME)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().post(ResetSearchView())
    }

    companion object {
        private const val NOTIFICATION_WORKER_NAME = "notificationWorker"
    }
}