package com.emmanuel.go4lunch.ui.listview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.databinding.FragmentListViewBinding
import com.emmanuel.go4lunch.utils.RESTAURANT_LIST_SAMPLE

class ListViewFragment : Fragment() {

    private lateinit var binding: FragmentListViewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListViewBinding.bind(view)

        binding.listViewRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ListViewAdapter(RESTAURANT_LIST_SAMPLE)
        }
    }
}