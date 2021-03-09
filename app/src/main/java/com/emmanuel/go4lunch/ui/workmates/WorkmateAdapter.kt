package com.emmanuel.go4lunch.ui.workmates

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.WorkmatesItemBinding
import com.emmanuel.go4lunch.utils.isSameDay
import com.squareup.picasso.Picasso
import java.util.*

class WorkmateAdapter :
    RecyclerView.Adapter<WorkmateAdapter.ViewHolder>() {
    private var mRestaurants = mutableListOf<Restaurant>()
    private var mWorkmates = mutableListOf<Workmate>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = WorkmatesItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(
        mWorkmates[position]
    )

    override fun getItemCount(): Int = mWorkmates.size

    inner class ViewHolder(val binding: WorkmatesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(workmate: Workmate) {
            var favoriteRestaurantName: String? = null
            for (restaurant in mRestaurants) {
                if (workmate.restaurantFavorite.equals(restaurant.id) && isSameDay(
                        workmate.favoriteDate,
                        Calendar.getInstance().time
                    )
                )
                    favoriteRestaurantName = restaurant.name.toString()
            }

            if (favoriteRestaurantName.isNullOrBlank()) {
                binding.workmateItemNameTextView.text = ""
                binding.workmateItemNameTextView.hint = "${workmate.name} hasn't decided yet"
                binding.workmateItemNameTextView.setTypeface(
                    binding.workmateItemNameTextView.typeface,
                    Typeface.ITALIC
                )
            } else {
                binding.workmateItemNameTextView.text = binding.root.context.getString(
                    R.string.fragment_workmates_name,
                    workmate.name,
                    favoriteRestaurantName
                )
                binding.workmateItemNameTextView.hint = ""
                binding.workmateItemNameTextView.setTypeface(
                    binding.workmateItemNameTextView.typeface,
                    Typeface.BOLD
                )
            }
            Picasso.get()
                .load(workmate.avatarURL)
                .resize(60, 60)
                .into(binding.workmatesItemImageView)
            if (workmate.restaurantFavorite != null && isSameDay(
                    workmate.favoriteDate,
                    Calendar.getInstance().time
                )
            ) {
                binding.containerWorkmatesItem.setOnClickListener {
                    val action =
                        WorkmatesFragmentDirections.actionWorkmatesFragmentToRestaurantDetail(
                            null,
                            workmate.restaurantFavorite.toString()
                        )
                    it.findNavController().navigate(action)

                    Navigation.createNavigateOnClickListener(R.id.action_workmatesFragment_to_restaurantDetail)
                }
            }
        }
    }

    fun updateWorkmateList(
        workmateList: List<Workmate>?, restaurantsList: List<Restaurant>?
    ) {

        workmateList?.let {
            mWorkmates.clear()
            mWorkmates.addAll(it)
        }
        if (restaurantsList?.size!! > 0) {
            mRestaurants.clear()
            mRestaurants.addAll(restaurantsList)
        }
        mWorkmates.sortBy { it.restaurantFavorite }
        mWorkmates.sortBy { it.favoriteDate }
        mWorkmates.reverse()
        notifyDataSetChanged()
    }
}