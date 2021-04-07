package com.emmanuel.go4lunch.ui.restaurantdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.WorkmatesItemBinding
import com.emmanuel.go4lunch.utils.CircleTransform
import com.squareup.picasso.Picasso

class RestaurantDetailAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Workmate>() {

        override fun areItemsTheSame(oldItem: Workmate, newItem: Workmate): Boolean {
            return oldItem.uid.equals(newItem.uid)
        }

        override fun areContentsTheSame(oldItem: Workmate, newItem: Workmate): Boolean {
            return oldItem.equals(newItem)
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return RestaurantDetailViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.workmates_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RestaurantDetailViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Workmate>) {
        differ.submitList(list)
    }

    class RestaurantDetailViewHolder
    constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(workmate: Workmate) = with(itemView) {
            val binding = WorkmatesItemBinding.bind(itemView)

            val translateAnim = AnimationUtils.loadAnimation(binding.root.context,R.anim.recyclerview_item_anim)
            binding.containerWorkmatesItem.animation = translateAnim
            binding.workmateItemNameTextView.text = binding.root.context.getString(
                R.string.detail_restaurant_joining_message,
                workmate.name
            )
            Picasso.get()
                .load(workmate.avatarURL)
                .transform(CircleTransform())
                .resize(60, 60)
                .into(binding.workmatesItemImageView)
        }
    }
}
