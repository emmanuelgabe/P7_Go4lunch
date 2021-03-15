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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.MainViewModel
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.FragmentRestaurantDetailBinding
import com.emmanuel.go4lunch.di.Injection
import com.emmanuel.go4lunch.utils.MAX_WITH_IMAGE
import com.emmanuel.go4lunch.utils.REQUEST_PERMISSIONS_CODE_CALL_PHONE
import com.emmanuel.go4lunch.utils.getPhotoUrlFromReference
import com.emmanuel.go4lunch.utils.isSameDay
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.math.roundToInt

class RestaurantDetailFragment : Fragment() {
    private lateinit var binding: FragmentRestaurantDetailBinding
    private lateinit var mCurrentWorkmate: Workmate
    private var mRestaurantDetail: NearByRestaurant? = null
    private var mWorkmates: List<Workmate>? = null
    private lateinit var mAdapter: RestaurantDetailAdapter
    private var factory = Injection.provideViewModelFactory()
    private lateinit var restaurantDetailViewModel: RestaurantDetailViewModel
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        restaurantDetailViewModel =
            ViewModelProvider(this, factory).get(RestaurantDetailViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        binding = FragmentRestaurantDetailBinding.bind(view)
        mAdapter = RestaurantDetailAdapter()
        restaurantDetailViewModel.init(
            arguments?.getString("restaurantId"),
            (arguments?.getSerializable("restaurantDetail") as? NearByRestaurant)
        )
        mainViewModel.workmatesLiveData.observe(viewLifecycleOwner, { workmates ->
            mWorkmates = workmates
            mCurrentWorkmate = mainViewModel.currentUserLiveData.value!!
            updateUi()

        })

        restaurantDetailViewModel.currentRestaurantsDetailLiveData.observe(
            viewLifecycleOwner,
            { restaurant ->
                mRestaurantDetail = restaurant
                updateUi()
                initUi(restaurant)
            })
        return view
    }

    private fun initUi(mCurrentRestaurant: NearByRestaurant) {
        binding.restaurant = mCurrentRestaurant
        if (mCurrentRestaurant.rating != null) {
            val rating = mCurrentRestaurant.rating.toFloat() * 3 / 5
            binding.fragmentRestaurantDetailRatingBar.rating = rating.roundToInt().toFloat()
        } else {
            binding.fragmentRestaurantDetailRatingBar.visibility = View.GONE
        }
        if (mCurrentRestaurant.photos?.get(0)?.photoReference != null) {
            Picasso.get()
                .load(
                    getPhotoUrlFromReference(
                        mCurrentRestaurant.photos[0].photoReference,
                        MAX_WITH_IMAGE
                    )
                )
                .into(binding.activityDetailRestaurantImage)
        } else {
            binding.activityDetailRestaurantImage.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.activityDetailRestaurantImage.setImageResource(R.drawable.ic_no_photography_24)
        }
        binding.fragmentDetailButtonCall.setOnClickListener {
            if (mCurrentRestaurant.phoneNumber != null) {
                if (mCurrentRestaurant.phoneNumber.isNotBlank()) {
                    if (isPermissionCallGranted()) {
                        callRestaurant()
                    } else {
                        requestPermissions(
                            arrayOf(Manifest.permission.CALL_PHONE),
                            REQUEST_PERMISSIONS_CODE_CALL_PHONE
                        )
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
            if (!mCurrentRestaurant.website.isNullOrBlank()) {
                val intent =
                    Intent(Intent.ACTION_VIEW).setData(Uri.parse(mCurrentRestaurant.website))
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
            restaurantDetailViewModel.updateLikeRestaurant(mCurrentWorkmate)
        }
        binding.fragmentDetailRestaurantFab.setOnClickListener {
            restaurantDetailViewModel.updateFavoriteRestaurant(mCurrentWorkmate, mWorkmates!!)
            // updateFavoriteRestaurant()
        }
    }

    private fun updateUi() {
        if (mRestaurantDetail != null && mWorkmates != null) {
            // update like button
            if (!mCurrentWorkmate.restaurantsIdLike.isNullOrEmpty() && mCurrentWorkmate.restaurantsIdLike!!.contains(
                    mRestaurantDetail!!.placeId
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
            if (mCurrentWorkmate.restaurantFavorite.equals(
                    mRestaurantDetail!!.placeId
                ) && isSameDay(
                    mCurrentWorkmate.favoriteDate,
                    Calendar.getInstance().time
                )
            ) {
                binding.fragmentDetailRestaurantFab.setImageResource(R.drawable.ic_check_favorite_restaurant)
            } else {
                binding.fragmentDetailRestaurantFab.setImageResource(R.drawable.ic_uncheck_favorite_restaurant)
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_CODE_CALL_PHONE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog()
            }
        }
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

    private fun isPermissionCallGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CALL_PHONE
    ) == PackageManager.PERMISSION_GRANTED

    private fun updateWorkmateList() {
        val workmatesIsJoining = mutableListOf<Workmate>()
        for (workmate in mWorkmates!!) {
            if (workmate.restaurantFavorite.equals(
                    mRestaurantDetail!!.placeId
                ) && isSameDay(
                    workmate.favoriteDate,
                    Calendar.getInstance().time
                )
            ) {
                workmatesIsJoining.add(workmate)
            }
        }
        mAdapter.updateWorkmateList(workmatesIsJoining.toList())
    }
}