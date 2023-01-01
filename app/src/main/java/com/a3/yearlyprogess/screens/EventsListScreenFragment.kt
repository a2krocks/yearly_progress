package com.a3.yearlyprogess.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.yearlyprogess.databinding.FragmentScreenListEventsBinding
import com.a3.yearlyprogess.eventManager.adapter.EventsListViewAdapter
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.eventManager.viewmodel.EventViewModel

class EventsListScreenFragment : Fragment() {


    private var _binding: FragmentScreenListEventsBinding? = null
    private lateinit var mEventViewModel: EventViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentScreenListEventsBinding.inflate(inflater, container, false)

        val eventAdapter = EventsListViewAdapter()
        binding.eventsRecyclerViewer.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        mEventViewModel = ViewModelProvider(this)[EventViewModel::class.java]
        mEventViewModel.readAllData.observe(viewLifecycleOwner) { events ->
            eventAdapter.setData(events)
        }

        binding.test.setOnClickListener {
            mEventViewModel.addEvent(
                Event(0,"test", "test desc", 5L, 10L)
            )
        }

        return binding.root

    }
}