package com.emmanuel.go4lunch.ui.listview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.databinding.RestaurantItemBinding


class ListViewAdapter(private val restaurants: List<Restaurant>) :
    RecyclerView.Adapter<ListViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RestaurantItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(restaurants[position])

    override fun getItemCount(): Int = restaurants.size

    inner class ViewHolder(val binding: RestaurantItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Restaurant) {
            binding.restaurant = item
/*
            Glide.with(binding.restaurantItemImageView.context)
                .load(item.avatar).override(60, 60)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.workmatesItemImageView)*/
        }
    }
}