package com.emmanuel.go4lunch.ui.workmates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.WorkmatesItemBinding

class WorkmateAdapter(private val items: List<Workmate>) :
    RecyclerView.Adapter<WorkmateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = WorkmatesItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: WorkmatesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Workmate) {
            binding.workmate = item

            Glide.with(binding.workmatesItemImageView.context)
                .load(item.avatar).override(60, 60)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.workmatesItemImageView);
            binding.containerWorkmatesItem.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_workmatesFragment_to_restaurantDetail)
            )
        }
    }
}