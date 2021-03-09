package com.emmanuel.go4lunch.ui.workmates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.FragmentWorkmatesBinding
import com.emmanuel.go4lunch.di.Injection

class WorkmatesFragment : Fragment() {

    private lateinit var binding: FragmentWorkmatesBinding
    private lateinit var mAdapter: WorkmateAdapter
    private var factory = Injection.provideViewModelFactory()
    private lateinit var workmateViewModel: WorkmateViewModel
    private lateinit var mRestaurantsList: List<Restaurant>
    private lateinit var mWorkmatesList: List<Workmate>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workmates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        workmateViewModel = ViewModelProvider(this, factory).get(WorkmateViewModel::class.java)
        binding = FragmentWorkmatesBinding.bind(view)
        binding.workmatesRecyclerView.layoutManager = LinearLayoutManager(activity)
        mAdapter = WorkmateAdapter()
        binding.workmatesRecyclerView.adapter = mAdapter
        workmateViewModel.workmatesLiveData.observe(viewLifecycleOwner, { workmates ->
            mWorkmatesList = workmates
            updateWorkmateList()
        })
        workmateViewModel.restaurantLiveData.observe(
            viewLifecycleOwner,
            { restaurants ->
                mRestaurantsList = restaurants
                updateWorkmateList()
            })
    }

    private fun updateWorkmateList() {
        if (this::mRestaurantsList.isInitialized && this::mWorkmatesList.isInitialized) {
            mAdapter.updateWorkmateList(mWorkmatesList, mRestaurantsList)
        }
    }

    override fun onResume() {
        super.onResume()
        workmateViewModel.getAllRestaurants()
        workmateViewModel.getAllWorkmate()
    }
}