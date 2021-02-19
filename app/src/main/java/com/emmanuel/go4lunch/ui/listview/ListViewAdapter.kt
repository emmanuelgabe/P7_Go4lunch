package com.emmanuel.go4lunch.ui.listview

import android.graphics.Color
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.databinding.RestaurantItemBinding
import com.emmanuel.go4lunch.utils.MAX_WITH_ICON
import com.emmanuel.go4lunch.utils.getPhotoUrlFromReference
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.math.roundToInt

class ListViewAdapter() :
    RecyclerView.Adapter<ListViewAdapter.ViewHolder>() {
    private var restaurants = mutableListOf<NearByRestaurant>()
    var mLastKnownLocation: Location? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RestaurantItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(restaurants.get(position))

    override fun getItemCount(): Int = restaurants.size

    inner class ViewHolder(val binding: RestaurantItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: NearByRestaurant) {

            binding.restaurant = restaurant
            if (restaurant.rating != null) {
                binding.restaurantItemRatingBar.rating = restaurant.rating.toFloat()
            } else {
                binding.restaurantItemRatingBar.visibility = View.GONE
            }

            if (restaurant.businessStatus == "CLOSED_TEMPORARILY" || restaurant.businessStatus == "CLOSED_PERMANENTLY") {
                binding.restaurantItemTimetableTextView.text =
                    binding.root.context.getString(R.string.fragment_list_view_close_statut_restaurant)
                binding.restaurantItemTimetableTextView.setTextColor(Color.RED)
            } else {
                    binding.restaurantItemTimetableTextView.text =
                        getOpeningHourDayFromWeekList(restaurant.openingHours?.weekdayText)
            }
            mLastKnownLocation?.let {
                val distance = FloatArray(1)
                Location.distanceBetween(
                    restaurant.geometry.location.lat,
                    restaurant.geometry.location.lng,
                    mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude, distance
                )
                binding.restaurantItemDistanceTextView.text =
                    binding.root.context.getString(R.string.fragment_list_view_distance, distance[0].roundToInt().toString())
            }
            if (restaurant.photos?.get(0)?.photoReference != null) {
                Picasso.get()
                    .load(
                        getPhotoUrlFromReference(
                            restaurant.photos.get(0).photoReference,
                            MAX_WITH_ICON
                        )
                    )
                    .resize(60, 60)
                    .into(binding.restaurantItemImage)
            } else {
                binding.restaurantItemImage.setImageResource(R.drawable.ic_no_photography_24)
            }
            binding.restaurantItemContainer.setOnClickListener {
                val action =
                    ListViewFragmentDirections.actionListViewFragmentToRestaurantDetail(restaurant)
                it.findNavController().navigate(action)
            }
        }

        fun getOpeningHourDayFromWeekList(weekDay: List<String>?): String {
            val openingHours = weekDay?.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
            if (openingHours.isNullOrBlank()) {
                return binding.root.context.getString(R.string.fragment_list_view_message_no_timetable)
            }
            return openingHours.substring(openingHours.indexOf(" ") + 1)
        }
    }

    fun updateRestaurantsList(
        restaurantsDetail: List<NearByRestaurant>,
        lastKnownLocation: Location?
    ) {
        mLastKnownLocation = lastKnownLocation
        restaurants.clear()
        restaurants.addAll(restaurantsDetail)
        notifyDataSetChanged()
    }
}