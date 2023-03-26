package com.demo.storedemo.ui.home

import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.icu.text.CaseMap.Title
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ContentView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.storedemo.App
import com.demo.storedemo.R
import com.demo.storedemo.databinding.FragmentHomeCategoryBinding
import com.demo.storedemo.model.AppInfo
import com.demo.storedemo.ui.AppDetailsActivity
import com.demo.storedemo.utils.createTmpDetailsFile
import com.demo.storedemo.utils.openApp
import timber.log.Timber



class TypeCategoryFragment : HomeBase() {


    private var appData = arrayListOf<CategoryInfo>()

    private var binding: FragmentHomeCategoryBinding? = null

    private lateinit var itemAdapter: AppItemAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val data = it.getParcelableArrayList<AppInfo>("apps")
            val categoryList = arrayListOf<String>()
            data?.forEach {
                val index = categoryList.indexOf(it.category)
                if (index >= 0){
                    appData.get(index).apps.add(it)
                }else{
                    appData.add(CategoryInfo(categoryList.size,it.category, arrayListOf()))
                    categoryList.add(it.category)
                }
            }
        }

        setInstallCb(appInstallCb)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeCategoryBinding.inflate(inflater,container,false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding?.appRecycler?.layoutManager = linearLayoutManager
        itemAdapter = AppItemAdapter(appData,linearLayoutManager,true,)
        binding?.appRecycler?.adapter = itemAdapter
        itemAdapter.onItemClick = {position,appInfo,type ->
            Timber.d("onItem Click $type ${appInfo.installed}")
            if (appInfo.installed || (type == 1 && isApkInstall(appInfo.packageName))){
                //openApp(requireContext(),appInfo.packageName)
                openDetails(appInfo)
            }else if (type == 0){
                installApk(appInfo,position)
            }else{
                openDetails(appInfo)
            }

        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        //super.onSaveInstanceState(outState)
    }


    private fun openDetails(appInfo: AppInfo){
        Timber.d("openDetails")
        //val path = createTmpDetailsFile(appInfo)
        Intent().apply {
            setClass(requireContext(),AppDetailsActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            putExtra("app",appInfo)
            startActivity(this)
            Timber.d("startApp Details")
        }
    }

    private fun isApkInstall(pkg: String): Boolean{
        try {
            return requireActivity().packageManager.getPackageInfo(pkg,0) != null
        }catch (e: NameNotFoundException){
            return false
        }

    }


    private val appInstallCb = object : InstallAppCb{

        override fun downloadStart(pkg: String) {
            val position = getPositionForPkg(pkg)
            itemAdapter.downloadStart(position,pkg)
        }

        override fun downloadProgress(pkg: String, progress: Long, total: Long) {
            val position = getPositionForPkg(pkg)
            itemAdapter.downloadProgress(position,pkg,progress, total)
        }

        override fun downloadCompleted(pkg: String, result: Boolean, savePath: String?) {
            val position = getPositionForPkg(pkg)
            itemAdapter.downloadCompleted(position, pkg, result)
        }

        override fun installStart(pkg: String?) {
            val position = getPositionForPkg(pkg!!)
            itemAdapter.installStart(position, pkg)
        }

        override fun installCompleted(pkg: String?, result: Boolean) {
            val position = getPositionForPkg(pkg!!)
            itemAdapter.installCompleted(position, pkg,result)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(apps: ArrayList<AppInfo>) =
            TypeCategoryFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("apps",apps)
                }
            }
    }
}