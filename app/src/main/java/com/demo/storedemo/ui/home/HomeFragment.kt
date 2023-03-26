package com.demo.storedemo.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.demo.storedemo.R
import com.demo.storedemo.databinding.FragmentHomeBinding
import com.demo.storedemo.download.DownloadCallback
import com.demo.storedemo.download.DownloadService
import com.demo.storedemo.model.AppInfo
import com.demo.storedemo.utils.openApp
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewModel:HomeViewModel

    private var isBind = false
    private var binder: DownloadService.DownBinder? = null

    var currFragment: Fragment? = null

    private val categoryApps = arrayListOf<AppInfo>()
    private val selectedApps = arrayListOf<AppInfo>()
    private val rankApps = arrayListOf<AppInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        startService()
        _binding?.loadView?.visibility = View.VISIBLE
        binding.homeContainer.visibility = View.GONE
        viewModel.observableApps.observe(viewLifecycleOwner){ apps ->
            Timber.d("size ${apps?.size}")
            binding.loadView.visibility = View.GONE
            if (apps.isNullOrEmpty()){
                binding.emptyView?.visibility = View.VISIBLE
                _binding?.homeContainer?.visibility = View.GONE
            }else{
                _binding?.homeContainer?.visibility = View.VISIBLE

                apps.forEach {
                    if (it.category == "热门排行榜"){
                        rankApps.add(it)
                    }else if (it.downloadUrl.contains(".apk")){
                        selectedApps.add(it)
                    }else if (it.downloadUrl.contains(".zip")){
                        categoryApps.add(it)
                    }
                }
            }
            changeTypePager(0)
        }
        return _binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchApps()
        bindService()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Timber.d("tab $tab  ${tab?.position}")
                changeTypePager(tab?.position ?:0)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

    }

    fun installApk(info: AppInfo){
        Timber.d("start download $isBind")
        if (isBind){
            binder?.getService()?.startDownload(info.packageName,info.name,info.downloadUrl)
        }
    }

    private fun changeTypePager(index: Int){
        val transaction = childFragmentManager.beginTransaction()
        val tag = "home-tag-$index"
        var find = childFragmentManager.findFragmentByTag(tag)
        Timber.d("curr $currFragment , find $find")
        if (index == 0 && currFragment == null){
            currFragment = TypeSelectedFragment.newInstance(selectedApps)
            transaction.add(R.id.homeContainer,currFragment!!,tag)
        }else if (find == null && currFragment != null) {
            var tmp = if (index == 1) TypeRankFragment.newInstance(rankApps) else TypeCategoryFragment.newInstance(categoryApps)
            transaction.hide(currFragment!!)
            transaction.add(R.id.homeContainer,tmp,tag)
            currFragment = tmp
        }else if (find != null && currFragment != null){
            transaction.hide(currFragment!!)
            transaction.show(find)
            currFragment = find
        }
        getCurrHomeBase()?.parentFragment = this
        transaction.commitNowAllowingStateLoss()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun startService(){
        requireActivity().startService(Intent().apply {
            setClass(requireContext(),DownloadService::class.java)
        })
    }


    private fun bindService(){
        if (!isBind){
            activity?.bindService(Intent().apply {
                setClass(requireContext(),DownloadService::class.java)
            },serviceConn, Context.BIND_AUTO_CREATE)
        }

    }


    private val downloadCallback = object : DownloadCallback{
        override fun downloadStart(pkg: String, downloadId: Long) {
            Timber.d("downloadStart $pkg")
            getCurrHomeBase()?.installCb?.downloadStart(pkg)
            //appAdapter.startDownload(pkg)
        }

        override fun downloadProgress(pkg: String, progress: Long, total: Long) {
            //Timber.d("downloadProgress $pkg")
            activity?.runOnUiThread {
                //appAdapter.updateDownloadProgress(pkg,progress, total)
                getCurrHomeBase()?.installCb?.downloadProgress(pkg,progress, total)
            }

        }

        override fun downloadComplete(pkg: String, savePath: String) {
            Timber.d("downloadComplete ")
            activity?.runOnUiThread{
                //appAdapter.downloadComplete(pkg)
                getCurrHomeBase()?.installCb?.downloadCompleted(pkg,true,savePath)
            }

        }

        override fun installStart(pkg: String?) {
            Timber.d("installStart $pkg ")
            activity?.runOnUiThread {
                //appAdapter.installStart(pkg)
                getCurrHomeBase()?.installCb?.installStart(pkg)
            }
        }

        override fun installResult(pkg: String?, success: Boolean) {
            Timber.d("installResult $pkg $success")
            activity?.runOnUiThread {
                //appAdapter.installResult(pkg,success)
                getCurrHomeBase()?.installCb?.installCompleted(pkg,success)
            }
        }

    }

    private fun getCurrHomeBase(): HomeBase?{
        return if (currFragment != null )currFragment as HomeBase else null
    }


    private val serviceConn = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("Service Connected")
            isBind = true
            binder = service as DownloadService.DownBinder
            binder?.getService()?.setDownloadCallback(downloadCallback)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBind = false
        }

    }
}