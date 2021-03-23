package com.emmanuel.go4lunch.ui.workmates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.MainViewModel
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.databinding.FragmentWorkmatesBinding
import com.emmanuel.go4lunch.di.Injection

class WorkmatesFragment : Fragment() {

    private lateinit var binding: FragmentWorkmatesBinding
    private lateinit var mAdapter: WorkmateAdapter
    private lateinit var workmateViewModel: WorkmateViewModel
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workmates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = Injection.provideViewModelFactory(requireContext())
        workmateViewModel = ViewModelProvider(this, factory).get(WorkmateViewModel::class.java)
        binding = FragmentWorkmatesBinding.bind(view)
        binding.workmatesRecyclerView.layoutManager = LinearLayoutManager(activity)
        mAdapter = WorkmateAdapter()
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
                mAdapter.updateWorkmateList(mainViewModel.workmatesLiveData.value, workmateViewModel.restaurantLiveData.value)
            } else {
                mAdapter.updateWorkmateList(workmateSearchList, workmateViewModel.restaurantLiveData.value)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        workmateViewModel.getAllRestaurants()
    }
}