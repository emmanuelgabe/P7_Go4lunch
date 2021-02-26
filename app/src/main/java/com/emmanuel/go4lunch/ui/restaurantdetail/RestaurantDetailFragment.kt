package com.emmanuel.go4lunch.ui.restaurantdetail

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.databinding.FragmentRestaurantDetailBinding
import com.emmanuel.go4lunch.utils.MAX_WITH_IMAGE
import com.emmanuel.go4lunch.utils.REQUEST_PERMISSIONS_CODE_CALL_PHONE
import com.emmanuel.go4lunch.utils.getPhotoUrlFromReference
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class RestaurantDetailFragment : Fragment() {
    private lateinit var binding: FragmentRestaurantDetailBinding
    private  var mCurrentRestaurant: NearByRestaurant? = null
    private lateinit var mCurrentUser:Workmate
    private lateinit var mWorkmates: List<Workmate>
    private lateinit var mAdapter: RestaurantDetailAdapter
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false)
        mCurrentRestaurant = (arguments?.getSerializable("restaurantDetail") as? NearByRestaurant)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        binding = FragmentRestaurantDetailBinding.bind(view)
        fetchWorkmates()
        return view
    }

    private fun initUi() {
        binding.restaurant = mCurrentRestaurant
        if (mCurrentRestaurant?.rating != null) {
            binding.fragmentRestaurantDetailRatingBar.rating = mCurrentRestaurant?.rating!!.toFloat()
        } else {
            binding.fragmentRestaurantDetailRatingBar.visibility = View.GONE
        }
        if (mCurrentRestaurant?.photos?.get(0)?.photoReference != null) {
            Picasso.get()
                .load(getPhotoUrlFromReference(mCurrentRestaurant!!.photos?.get(0)?.photoReference,MAX_WITH_IMAGE))
                .into(binding.activityDetailRestaurantImage)
        } else {
            binding.activityDetailRestaurantImage.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.activityDetailRestaurantImage.setImageResource(R.drawable.ic_no_photography_24)
        }
        binding.fragmentDetailButtonCall.setOnClickListener {
            if (mCurrentRestaurant?.phoneNumber != null) {
                if (mCurrentRestaurant?.phoneNumber!!.isNotBlank()) {
                    if (isPermissionCallGranted()) {
                        callRestaurant()
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),arrayOf(Manifest.permission.CALL_PHONE),
                            REQUEST_PERMISSIONS_CODE_CALL_PHONE
                        )
                    }
                }
            } else {
                Toast.makeText(binding.root.context,getString(R.string.restaurant_detail_fragment_message_no_number_found),Toast.LENGTH_LONG).show()
            }
        }
        mAdapter = RestaurantDetailAdapter()
        binding.fragmentRestaurantDetailRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.fragmentRestaurantDetailRecyclerView.adapter = mAdapter

        binding.detailFragmentButtonWebsite.setOnClickListener {
            if (!mCurrentRestaurant?.website.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(mCurrentRestaurant?.website))
                startActivity(intent)
            }else{
                Toast.makeText(binding.root.context,
                    getString(R.string.restaurant_detail_fragment_message_no_web_site_found),
                    Toast.LENGTH_LONG).show()
            }
        }
        binding.fragmentDetailButtonLike.setOnClickListener {
            updateLikeRestaurant()
        }
        binding.fragmentDetailRestaurantFab.setOnClickListener {
            updateFavoriteRestaurant()
        }
    }

    private fun updateLikeRestaurant() {
        CoroutineScope(IO).launch {
            launch {
                val newLikeList = mutableListOf<String>()
                mCurrentUser.restaurantsIdLike?.let {
                    newLikeList.addAll(mCurrentUser.restaurantsIdLike!!)
                    if (mCurrentUser.restaurantsIdLike!!.contains(mCurrentRestaurant?.placeId))
                    {
                        newLikeList.remove(mCurrentRestaurant?.placeId.toString())
                    }else{
                        newLikeList.add(mCurrentRestaurant?.placeId.toString())
                    }
                }
                val updatedWorkmate = mCurrentUser
                updatedWorkmate.restaurantsIdLike = newLikeList
                WorkmateRepository.updateWorkmate(updatedWorkmate)
            }.join()
            fetchWorkmates()
        }
    }

    private fun fetchWorkmates() {
        CoroutineScope(IO).launch {
            val executionTime = measureTimeMillis {
                // if detail fragment is open from workmate fragment or mal fragment the detail restaurant is not fetch
                if (mCurrentRestaurant == null) {
                    mCurrentRestaurant =
                        RestaurantRepository.getDetailRestaurant(arguments?.getString("restaurantId")!!)
                }
                mWorkmates = WorkmateRepository.getAllWorkmate()
                launch {
                    val mAuth = FirebaseAuth.getInstance()
                    for (workmate in mWorkmates){
                        if (workmate.uid == mAuth.uid){
                            mCurrentUser = workmate
                        }
                    }
                }.join()
                withContext(Main) {
                    initUi()
                    updateUi()
                }
            }
            Log.d(TAG, "fetchNearWorkmates and update ui in : ${executionTime}ms")
        }
    }

    private fun updateUi() {
       // update like button
        binding.fragmentDetailButtonLike.setCompoundDrawablesWithIntrinsicBounds(
            null,
            ContextCompat.getDrawable(requireActivity(),R.drawable.ic_baseline_thumb_up_off_alt_24), null, null
        )
        if (mCurrentUser.restaurantsIdLike != null) {
            if (mCurrentUser.restaurantsIdLike!!.contains(mCurrentRestaurant?.placeId))
                binding.fragmentDetailButtonLike.setCompoundDrawablesWithIntrinsicBounds(
                    null,ContextCompat.getDrawable(requireActivity(),R.drawable.ic_baseline_thumb_up_alt_24), null, null)
            else
                binding.fragmentDetailButtonLike.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(requireActivity(), R.drawable.ic_baseline_thumb_up_off_alt_24), null, null)
        }
        // update favorite button
        if (mCurrentUser.restaurantFavorite.equals(mCurrentRestaurant?.placeId)) {
            binding.fragmentDetailRestaurantFab.setImageResource(R.drawable.ic_check_favorite_restaurant)
        } else {
            binding.fragmentDetailRestaurantFab.setImageResource(R.drawable.ic_uncheck_favorite_restaurant)
        }
        updateWorkmateList()
    }

    private fun updateFavoriteRestaurant() {
        CoroutineScope(IO).launch {
            launch {
                val idRestaurantToDeleteIfNeverUse = mCurrentUser.restaurantFavorite.toString()
                val updatedWorkmate = mCurrentUser
                if (mCurrentUser.restaurantFavorite.equals(mCurrentRestaurant?.placeId)) {
                    updatedWorkmate.restaurantFavorite = null
                } else {
                    launch {
                        WorkmateRepository.addRestaurant(Restaurant(mCurrentRestaurant?.placeId!!,mCurrentRestaurant?.name))
                    }.join()
                    updatedWorkmate.restaurantFavorite = mCurrentRestaurant?.placeId
                }
                launch {
                    WorkmateRepository.updateWorkmate(updatedWorkmate)
                }.join()
                launch {
                    if (restaurantIsNeverUse(idRestaurantToDeleteIfNeverUse)) {
                        WorkmateRepository.deleteRestaurant(idRestaurantToDeleteIfNeverUse)
                    }
                }.join()
            }.join()
            fetchWorkmates()
        }
    }

    private fun restaurantIsNeverUse(IdRestaurantToDeleteIfNeverUse: String): Boolean {
        var isNeverUse = true
        for (workmate in mWorkmates){
            if (workmate.restaurantFavorite.equals(IdRestaurantToDeleteIfNeverUse))
                isNeverUse = false
        }
        return isNeverUse
    }

    private fun callRestaurant() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:${mCurrentRestaurant?.phoneNumber!!.replace("\\s".toRegex(), "")}")
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

    private fun isPermissionCallGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    private fun updateWorkmateList() {
        val workmatesIsJoining = mutableListOf<Workmate>()
        for (workmate in mWorkmates) {
            if (workmate.restaurantFavorite.equals(mCurrentRestaurant?.placeId)) {
                workmatesIsJoining.add(workmate)
            }
        }
        mAdapter.updateWorkmateList(workmatesIsJoining.toList())
    }

    companion object {
        const val TAG = "RestaurantDetailFrag"
    }
}