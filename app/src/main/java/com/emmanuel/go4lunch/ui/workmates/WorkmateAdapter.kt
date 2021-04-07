package com.emmanuel.go4lunch.ui.workmates

import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.RestaurantDetail
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.WorkmatesItemBinding
import com.emmanuel.go4lunch.utils.CircleTransform
import com.emmanuel.go4lunch.utils.isSameDay
import com.squareup.picasso.Picasso
import java.util.*

class WorkmateAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mRestaurants = mutableListOf<RestaurantDetail>()

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
        return WorkamteViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.workmates_item,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WorkamteViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Workmate>,restaurantList: List<RestaurantDetail>) {
        mRestaurants.clear()
        mRestaurants.addAll(restaurantList)
        differ.submitList(list)
    }

    inner class WorkamteViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {
        val binding = WorkmatesItemBinding.bind(itemView)

        fun bind(workmate: Workmate) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(workmate)
            }

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
                binding.workmateItemNameTextView.hint = binding.root.context.getString(
                    R.string.fragment_workmates_not_decide,
                    workmate.name
                )
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
                .transform(CircleTransform())
                .into(binding.workmatesItemImageView)
            val translateAnim = AnimationUtils.loadAnimation(binding.root.context,R.anim.recyclerview_item_anim)
            binding.containerWorkmatesItem.animation = translateAnim
        }
    }

    interface Interaction {
        fun onItemSelected(workmate: Workmate)
    }
}
