package com.emmanuel.go4lunch.ui.restaurantdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.databinding.FragmentRestaurantDetailBinding
import com.emmanuel.go4lunch.utils.WORKMATE_LIST_SAMPLE

class RestaurantDetailFragment : Fragment() {

    private lateinit var binding: FragmentRestaurantDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        return inflater.inflate(R.layout.fragment_restaurant_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRestaurantDetailBinding.bind(view)

        binding.fragmentRestaurantDetailRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = RestaurantDetailAdapter(WORKMATE_LIST_SAMPLE)
        }
    }
}
