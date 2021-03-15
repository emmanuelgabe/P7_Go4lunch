package com.emmanuel.go4lunch.ui.restaurantdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.WorkmatesItemBinding
import com.squareup.picasso.Picasso

class RestaurantDetailAdapter :
    RecyclerView.Adapter<RestaurantDetailAdapter.ViewHolder>() {
    private var mWorkmatesIsJoining = mutableListOf<Workmate>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = WorkmatesItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(mWorkmatesIsJoining[position])

    override fun getItemCount(): Int = mWorkmatesIsJoining.size

    fun updateWorkmateList(workmateList: List<Workmate>) {
        mWorkmatesIsJoining.clear()
        mWorkmatesIsJoining.addAll(workmateList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: WorkmatesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(workmate: Workmate) {

            binding.workmateItemNameTextView.text = binding.root.context.getString(
                R.string.detail_restaurant_joining_message,
                workmate.name
            )
            Picasso.get()
                .load(workmate.avatarURL)
                .resize(60, 60)
                .into(binding.workmatesItemImageView)
        }
    }
}