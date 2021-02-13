package com.emmanuel.go4lunch.ui.restaurantdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.databinding.FragmentRestaurantDetailBinding

class RestaurantDetailFragment : Fragment() {
    private lateinit var restaurantId:String
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
            // TODO add list
         //   adapter = RestaurantDetailAdapter()
        }
        restaurantId = arguments?.getString("restaurantId").toString()
        Toast.makeText(requireContext(),restaurantId,Toast.LENGTH_LONG).show()
    }
}
