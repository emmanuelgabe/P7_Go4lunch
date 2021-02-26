package com.emmanuel.go4lunch.ui.workmates

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.databinding.FragmentWorkmatesBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlin.system.measureTimeMillis

class WorkmatesFragment : Fragment() {

    private lateinit var binding: FragmentWorkmatesBinding
    private lateinit var mAdapter:WorkmateAdapter
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workmates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWorkmatesBinding.bind(view)
        binding.workmatesRecyclerView.layoutManager = LinearLayoutManager(activity)
        mAdapter = WorkmateAdapter()
        binding.workmatesRecyclerView.adapter = mAdapter
        }

    private fun fetchWorkmates() {
        CoroutineScope(IO).launch {
            val executionTime = measureTimeMillis {
                val workmates = async {
                    WorkmateRepository.getAllWorkmate()
                }
                val restaurants= async {
                    WorkmateRepository.getAllRestaurants()
                }
                updateWorkmateList(workmates.await(),restaurants.await())
            }
            Log.d(TAG, "fetchNearWorkmates and update ui in : ${executionTime}ms")
        }
    }
    private suspend fun updateWorkmateList(workmates: List<Workmate>, restaurants: List<Restaurant>) {
        withContext(Dispatchers.Main) {
            mAdapter.updateWorkmateList(workmates, restaurants)
        }
    }
    companion object {
        const val TAG = "WorkmatesFragment "
    }

    override fun onResume() {
        super.onResume()
        fetchWorkmates()
    }
}