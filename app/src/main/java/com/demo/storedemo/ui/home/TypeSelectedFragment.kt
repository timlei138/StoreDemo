package com.demo.storedemo.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.storedemo.R
import com.demo.storedemo.databinding.FragmentHomeSelectedBinding
import com.demo.storedemo.model.AppInfo
import timber.log.Timber


class TypeSelectedFragment : HomeBase() {


    private var _binding: FragmentHomeSelectedBinding? = null

    private val selectedApps = arrayListOf<CategoryInfo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val data = it.getParcelableArrayList<AppInfo>("apps")
            val categoryList = arrayListOf<String>()
            val tmp = arrayListOf<CategoryInfo>()
            data?.forEach {
                if (it.category.isNotEmpty()){
                    val index = categoryList.indexOf(it.category)
                    Timber.d("${it.category} ,$index")
                    if (index >= 0){
                        tmp.get(index).apps.add(it)
                    }else{
                        tmp.add(CategoryInfo(categoryList.size,it.category, arrayListOf()))
                        categoryList.add(it.category)
                    }
                }
            }

            selectedApps.addAll(tmp.filter {
                it.apps.isNotEmpty() && it.category.isNotEmpty()
            })

            Timber.d("select apps ${selectedApps.size}")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeSelectedBinding.inflate(inflater,container,false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        _binding?.adLayout?.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
//        _binding?.adLayout?.adapter = SimpleAdAdapter()
        applyAdItems()

        val appRecycler = view.findViewById<RecyclerView>(R.id.selectedAppsList)
        val linearLayoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL,false)
        appRecycler.layoutManager = linearLayoutManager
        appRecycler.adapter = AppItemAdapter(selectedApps,linearLayoutManager)


    }




    private fun applyAdItems(){
        val items = arrayOf(R.drawable.ad_00,R.drawable.ad_01,R.drawable.ad_02,R.drawable.ad_03,R.drawable.ad_04)
        items.forEachIndexed { index, i ->
            val adImage = ImageView(requireContext())
            adImage.setImageResource(items[index])
            val width = resources.getDimensionPixelOffset(R.dimen.gallery_item_width)
            val height = resources.getDimensionPixelOffset(R.dimen.gallery_item_height)
            val params = LinearLayout.LayoutParams(width,height).also {
                it.setMargins(15,0,15,0)
            }
            _binding?.adLayout?.addView(adImage,params)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(apps: ArrayList<AppInfo>) =
            TypeSelectedFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("apps",apps)
                }
            }
    }

}