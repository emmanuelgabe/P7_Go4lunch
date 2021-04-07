package com.emmanuel.go4lunch.ui.listview

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.RestaurantDetail
import com.emmanuel.go4lunch.databinding.RestaurantItemBinding
import com.emmanuel.go4lunch.utils.MAX_WITH_ICON
import com.emmanuel.go4lunch.utils.getPhotoUrlFromReference
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.math.roundToInt

class ListViewAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var  lastKnownLocation: Location? = null

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RestaurantDetail>() {

        override fun areItemsTheSame(
            oldItem: RestaurantDetail,
            newItem: RestaurantDetail
        ): Boolean {
            return  oldItem.id.equals(newItem.id)
        }

        override fun areContentsTheSame(
            oldItem: RestaurantDetail,
            newItem: RestaurantDetail
        ): Boolean {
         return  oldItem.equals(newItem)
        }

        override fun getChangePayload(oldItem: RestaurantDetail, newItem: RestaurantDetail): Any {
            val bundle = Bundle()
            newItem.workmateCount?.let {
                if (newItem.workmateCount?.equals(oldItem) == false) {
                    bundle.putInt("count", newItem.workmateCount!!)
                }
            }
            return bundle
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.restaurant_item,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,position: Int,payloads: MutableList<Any>) {
        when (holder) {
            is ViewHolder -> {
                if (payloads.isEmpty())
                    super.onBindViewHolder(holder, position, payloads)
                else {
                    val bundle = payloads.get(0) as Bundle
                    val count: Int = bundle.get("count") as Int
                    holder.bindItemCount(count)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitRestaurantDetailList(list: List<RestaurantDetail>, lastLocation:Location) {
        lastKnownLocation = lastLocation
        differ.submitList(list)
    }

    inner class ViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        val binding = RestaurantItemBinding.bind(itemView)

        fun bindItemCount(workmateCount: Int?){
            binding.restaurantItemWorkmatesNumberTextView.text = binding.root.context.getString(
                R.string.fragment_list_view_workmate_number,
                workmateCount.toString()
            )
            val translateAnim = AnimationUtils.loadAnimation(binding.root.context,R.anim.recyclerview_item_anim)
            binding.restaurantItemWorkmatesNumberTextView.animation = translateAnim
        }

        fun bind(restaurantDetail: RestaurantDetail) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(restaurantDetail)
            }

            binding.restaurant = restaurantDetail
           if (restaurantDetail.rating != null) {
               val rating = restaurantDetail.rating.toFloat() * 3 / 5
               binding.restaurantItemRatingBar.rating = rating.roundToInt().toFloat()
           } else {
               binding.restaurantItemRatingBar.rating = 0F
           }

            if (restaurantDetail.businessStatus == "CLOSED_TEMPORARILY" || restaurantDetail.businessStatus == "CLOSED_PERMANENTLY") {
                binding.restaurantItemTimetableTextView.text =
                    binding.root.context.getString(R.string.fragment_list_view_close_statut_restaurant)
                binding.restaurantItemTimetableTextView.setTextColor(Color.RED)
            } else {
                binding.restaurantItemTimetableTextView.text =
                    getOpeningHourDayFromWeekList(restaurantDetail, binding)
            }

            lastKnownLocation?.let {
                val distance = FloatArray(1)
                Location.distanceBetween(
                    restaurantDetail.lat!!,
                    restaurantDetail.lng!!,
                    lastKnownLocation!!.latitude, lastKnownLocation!!.longitude, distance
                )
                binding.restaurantItemDistanceTextView.text =
                    binding.root.context.getString(
                        R.string.fragment_list_view_distance,
                        distance[0].roundToInt().toString()
                    )
            }
            if (restaurantDetail.photoReference != null) {
                Picasso.get()
                    .load(
                        getPhotoUrlFromReference(
                            restaurantDetail.photoReference,
                            MAX_WITH_ICON
                        )
                    )
                    .resize(60, 60)
                    .into(binding.restaurantItemImage)
            } else {
                binding.restaurantItemImage.setImageResource(R.drawable.ic_no_photography_24)
            }
            binding.restaurantItemWorkmatesNumberTextView.text = binding.root.context.getString(
                R.string.fragment_list_view_workmate_number,
                restaurantDetail.workmateCount.toString()
            )
            val translateAnim = AnimationUtils.loadAnimation(binding.root.context,R.anim.recyclerview_item_anim)
            binding.restaurantItemContainer.animation = translateAnim

        }

        private fun getOpeningHourDayFromWeekList(
            restaurant: RestaurantDetail,
            binding: RestaurantItemBinding
        ): String {
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

    interface Interaction {
        fun onItemSelected(item: RestaurantDetail)
    }
}
