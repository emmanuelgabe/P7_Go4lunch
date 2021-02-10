package com.emmanuel.go4lunch.ui.workmates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.databinding.FragmentWorkmatesBinding

class WorkmatesFragment : Fragment() {

    private lateinit var binding: FragmentWorkmatesBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workmates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWorkmatesBinding.bind(view)

        binding.workmatesRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            // TODO add list
            // adapter = WorkmateAdapter()
        }
    }
}