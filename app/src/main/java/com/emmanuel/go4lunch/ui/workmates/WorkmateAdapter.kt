package com.emmanuel.go4lunch.ui.workmates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.WorkmatesItemBinding
import com.squareup.picasso.Picasso

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
            Picasso.get()
                .load(item.avatarURL)
                .resize(60, 60)
                .into(binding.workmatesItemImageView)

            binding.containerWorkmatesItem.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_workmatesFragment_to_restaurantDetail)
            )
        }
    }
}