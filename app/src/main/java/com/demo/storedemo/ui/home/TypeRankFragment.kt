package com.demo.storedemo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.storedemo.R
import com.demo.storedemo.databinding.FragmentHomeRankBinding
import com.demo.storedemo.model.AppInfo
import timber.log.Timber

class TypeRankFragment : HomeBase() {


    private val rankApps = arrayListOf<AppInfo>()

    private var binding: FragmentHomeRankBinding? = null

    private lateinit var rankAdapter: AppRankAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val data = it.getParcelableArrayList<AppInfo>("apps")
            data?.forEach {
                if (it.downloadUrl.isNotEmpty()){
                    rankApps.add(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeRankBinding.inflate(inflater,container,false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rankAdapter = AppRankAdapter(rankApps)
        binding?.rankRecycler?.adapter = rankAdapter
        binding?.rankRecycler?.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
    }

    companion object {

        @JvmStatic
        fun newInstance(apps: ArrayList<AppInfo>) =
            TypeRankFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("apps",apps)
                }
            }
    }
}