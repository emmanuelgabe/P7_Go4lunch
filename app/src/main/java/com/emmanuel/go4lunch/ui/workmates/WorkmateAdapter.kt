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

class WorkmateAdapter(private val workmateItemListener: WorkmateItemListener? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mRestaurants = mutableListOf<RestaurantDetail>()

    private val diffCallBack = object : DiffUtil.ItemCallback<Workmate>() {
        override fun areItemsTheSame(oldItem: Workmate, newItem: Workmate): Boolean {
            return oldItem.uid == newItem.uid
        }
        override fun areContentsTheSame(oldItem: Workmate, newItem: Workmate): Boolean {
           return oldItem == newItem
        }
    }
    private val differ = AsyncListDiffer(this, diffCallBack)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WorkmateViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }
    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WorkmateViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.workmates_item,
                parent,
                false
            ),
            workmateItemListener
        )
    }





    fun submitList(workmates: MutableList<Workmate>, restaurantList: List<RestaurantDetail>) {
        workmates.sortBy { it.restaurantFavorite }
        workmates.sortBy { it.favoriteDate }
        workmates.reverse()
        mRestaurants.clear()
        mRestaurants.addAll(restaurantList)
        differ.submitList(workmates)
    }

    inner class WorkmateViewHolder
    constructor(
        itemView: View,
        private val interaction: WorkmateItemListener?
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

    interface WorkmateItemListener {
        fun onItemSelected(workmate: Workmate)
    }
}
