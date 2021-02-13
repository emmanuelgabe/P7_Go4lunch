package com.emmanuel.go4lunch.ui.listview

import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emmanuel.go4lunch.BuildConfig
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.databinding.RestaurantItemBinding
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.math.roundToInt


class ListViewAdapter(private val restaurants: MutableList<NearByRestaurant>) :
    RecyclerView.Adapter<ListViewAdapter.ViewHolder>() {
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
            restaurant.rating.let {
                binding.restaurantItemRatingBar.rating = it?.toFloat()!!
            }
            restaurant.openingHours?.weekdayText.let {
                binding.restaurantItemTimetableTextView.text =
                    getOpeningHourDayFromWeekList(it!!)
            }
            mLastKnownLocation.let {
                val distance = FloatArray(1)
                Location.distanceBetween(
                    restaurant.geometry.location.lat,
                    restaurant.geometry.location.lng,
                    mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude, distance
                )

                binding.restaurantItemDistanceTextView.text = "${distance[0].roundToInt()}m"
            }
            restaurant.photos?.let {
                Picasso.get()
                    .load(getPhotoUrlFromReference(restaurant.photos.get(0).photoReference, 60))
                    .resize(60,60)
                    .into(binding.restaurantItemImage)
            }

        }

        private fun getPhotoUrlFromReference(reference: String?, maxWith: Int): String {
            return "https://maps.googleapis.com/maps/api/place/photo?" +
                    "maxwidth=$maxWith&" +
                    "photoreference=$reference" +
                    "&key=${BuildConfig.GOOGLE_MAP_API_KEY}"
        }

        fun getOpeningHourDayFromWeekList(weekDay: List<String>): String {
            val openingHours = weekDay.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
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