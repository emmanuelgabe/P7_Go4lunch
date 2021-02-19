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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.databinding.FragmentRestaurantDetailBinding
import com.emmanuel.go4lunch.utils.MAX_WITH_IMAGE
import com.emmanuel.go4lunch.utils.REQUEST_PERMISSIONS_CODE_CALL_PHONE
import com.emmanuel.go4lunch.utils.getPhotoUrlFromReference
import com.squareup.picasso.Picasso

class RestaurantDetailFragment : Fragment() {
    private lateinit var restaurantId: String
    private lateinit var binding: FragmentRestaurantDetailBinding
    private lateinit var restaurant: NearByRestaurant
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false)
        restaurant = (arguments?.getSerializable("RestaurantDetail") as? NearByRestaurant)!!
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        binding = FragmentRestaurantDetailBinding.bind(view)
        binding.restaurant = restaurant

        if (restaurant.rating != null) {
            binding.fragmentRestaurantDetailRatingBar.rating = restaurant.rating!!.toFloat()
        } else {
            binding.fragmentRestaurantDetailRatingBar.visibility = View.GONE
        }
        if (restaurant.photos?.get(0)?.photoReference != null) {
            Picasso.get()
                .load(
                    getPhotoUrlFromReference(
                        restaurant.photos?.get(0)?.photoReference,
                        MAX_WITH_IMAGE
                    )
                )
                .into(binding.activityDetailRestaurantImage)
        } else {
            binding.activityDetailRestaurantImage.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.activityDetailRestaurantImage.setImageResource(R.drawable.ic_no_photography_24)
        }
        binding.fragmentDetailButtonCall.setOnClickListener {
            if (restaurant.phoneNumber != null) {
                if (restaurant.phoneNumber!!.isNotBlank()) {
                    if (isPermissionCallGranted()) {
                        callRestaurant()
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.CALL_PHONE),
                            REQUEST_PERMISSIONS_CODE_CALL_PHONE
                        )
                    }

                }
            } else {
                Toast.makeText(binding.root.context,getString(R.string.restaurant_detail_fragment_message_no_number_found),Toast.LENGTH_LONG).show()
                //TODO add fetch detail restaurant with formatted_phone_number field
                // Phone number is null if detail fragment is display from map view
                // default = n/a
            }
        }
        binding.fragmentRestaurantDetailRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            // TODO add list
            //   adapter = RestaurantDetailAdapter()
        }
        binding.detailFragmentButtonWebsite.setOnClickListener {
            if (!restaurant.website.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(restaurant.website))
                startActivity(intent)
            }else{
                Toast.makeText(binding.root.context,
                    getString(R.string.restaurant_detail_fragment_message_no_web_site_found),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return view
    }

    private fun callRestaurant() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:${restaurant.phoneNumber!!.replace("\\s".toRegex(), "")}")
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
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
    fun isPermissionCallGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CALL_PHONE
    ) == PackageManager.PERMISSION_GRANTED
}