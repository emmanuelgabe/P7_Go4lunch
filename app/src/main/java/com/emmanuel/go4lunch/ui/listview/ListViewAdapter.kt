package com.emmanuel.go4lunch.ui.listview

import android.graphics.Color
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.database.model.RestaurantDetail
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.RestaurantItemBinding
import com.emmanuel.go4lunch.utils.MAX_WITH_ICON
import com.emmanuel.go4lunch.utils.getPhotoUrlFromReference
import com.emmanuel.go4lunch.utils.isSameDay
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.math.roundToInt

class ListViewAdapter :
    RecyclerView.Adapter<ListViewAdapter.ViewHolder>() {
    private var mRestaurants = mutableListOf<RestaurantDetail>()
    private var mLastKnownLocation: Location? = null
    private var mWorkmates = mutableListOf<Workmate>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RestaurantItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mRestaurants[position])
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int = mRestaurants.size

    inner class ViewHolder(val binding: RestaurantItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: RestaurantDetail) {

            binding.restaurant = restaurant
            if (restaurant.rating != null) {
                val rating = restaurant.rating.toFloat() * 3 / 5
                binding.restaurantItemRatingBar.rating = rating.roundToInt().toFloat()
            } else {
                binding.restaurantItemRatingBar.visibility = View.GONE
            }

            if (restaurant.businessStatus == "CLOSED_TEMPORARILY" || restaurant.businessStatus == "CLOSED_PERMANENTLY") {
                binding.restaurantItemTimetableTextView.text =
                    binding.root.context.getString(R.string.fragment_list_view_close_statut_restaurant)
                binding.restaurantItemTimetableTextView.setTextColor(Color.RED)
            } else {
                binding.restaurantItemTimetableTextView.text =
                    getOpeningHourDayFromWeekList(restaurant)
            }
            mLastKnownLocation?.let {
                val distance = FloatArray(1)
                Location.distanceBetween(
                    restaurant.lat,
                    restaurant.lng,
                    mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude, distance
                )
                binding.restaurantItemDistanceTextView.text =
                    binding.root.context.getString(
                        R.string.fragment_list_view_distance,
                        distance[0].roundToInt().toString()
                    )
            }
            if (restaurant.photoReference != null) {
                Picasso.get()
                    .load(
                        getPhotoUrlFromReference(
                            restaurant.photoReference,
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
                    ListViewFragmentDirections.actionListViewFragmentToRestaurantDetail(restaurant.id)
                it.findNavController().navigate(action)
            }
            var workmateCount = 0
            for (workmate in mWorkmates) {
                if (workmate.restaurantFavorite.equals(restaurant.id) && isSameDay(
                        workmate.favoriteDate,
                        Calendar.getInstance().time
                    )
                )
                    workmateCount++
            }
            binding.restaurantItemWorkmatesNumberTextView.text = binding.root.context.getString(
                R.string.fragment_list_view_workmate_number,
                workmateCount.toString()
            )
        }

        private fun getOpeningHourDayFromWeekList(restaurant: RestaurantDetail): String {
            val openingHours: String? = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                1 -> restaurant.weekdayText1
                2 -> restaurant.weekdayText2
                3 -> restaurant.weekdayText3
                4 -> restaurant.weekdayText4
                5 -> restaurant.weekdayText5
                6 -> restaurant.weekdayText6
                7 -> restaurant.weekdayText7
                else -> null
            }

            if (openingHours.isNullOrBlank()) {
                return binding.root.context.getString(R.string.fragment_list_view_message_no_timetable)
            }
            return openingHours.substring(openingHours.indexOf(" ") + 1)
        }
    }

    fun updateRestaurantsList(
        restaurantsDetail: List<RestaurantDetail>,
        lastKnownLocation: Location?,
        workmatesList: List<Workmate>
    ) {
        mLastKnownLocation = lastKnownLocation
        mRestaurants.clear()
        mRestaurants.addAll(restaurantsDetail)
        mWorkmates.clear()
        mWorkmates.addAll(workmatesList)
        notifyDataSetChanged()
    }
}