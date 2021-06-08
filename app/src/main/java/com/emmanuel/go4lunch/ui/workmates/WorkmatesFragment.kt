package com.emmanuel.go4lunch.ui.workmates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.App
import com.emmanuel.go4lunch.MainViewModel
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.FragmentWorkmatesBinding
import com.emmanuel.go4lunch.di.ViewModelFactory
import javax.inject.Inject

class WorkmatesFragment : Fragment(),WorkmateAdapter.WorkmateItemListener {
    @Inject lateinit var factory: ViewModelFactory
    private lateinit var binding: FragmentWorkmatesBinding
    private lateinit var mAdapter: WorkmateAdapter
    private lateinit var workmateViewModel: WorkmateViewModel
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.app().appComponent.inject(this)
    }
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
        mAdapter = WorkmateAdapter(this)
        binding.workmatesRecyclerView.adapter = mAdapter
        initObserver()
        updateWorkmateList()
    }

    private fun initObserver() {
        mainViewModel.workmatesLiveData.observe(viewLifecycleOwner, {
            updateWorkmateList()
        })
        workmateViewModel.restaurantLiveData.observe(viewLifecycleOwner, {
            updateWorkmateList()
        })
        mainViewModel.textSearchInput.observe(viewLifecycleOwner, { workmateSearch ->
            if (workmateSearch.isNotBlank()) {
                val workmateSearchList = mutableListOf<Workmate>()
                for (workmate in mainViewModel.workmatesLiveData.value!!) {
                    if (workmate.name!!.contains(workmateSearch, true)) {
                        workmateSearchList.add(workmate)
                    }
                }
                updateWorkmateList(workmateSearchList)
            } else {
                updateWorkmateList()
            }
        })
    }

    private fun updateWorkmateList(workmateSearchList: List<Workmate>? = null) {
        if (workmateViewModel.restaurantLiveData.value != null && mainViewModel.workmatesLiveData.value != null) {
            if (workmateSearchList == null ){
                mAdapter.submitList(
                    mainViewModel.workmatesLiveData.value!! as MutableList<Workmate>,
                    workmateViewModel.restaurantLiveData.value!!
                )
            } else {
                mAdapter.submitList(
                    workmateSearchList as MutableList<Workmate>,
                    workmateViewModel.restaurantLiveData.value!!
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        workmateViewModel.getAllRestaurants()
    }

    override fun onItemSelected(workmate: Workmate) {
        val action =
            WorkmatesFragmentDirections.actionWorkmatesFragmentToRestaurantDetail(
                workmate.restaurantFavorite.toString()
            )
        findNavController().navigate(action)
    }
}