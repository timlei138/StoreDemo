package com.demo.storedemo.ui.home

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.storedemo.App.Companion.appContext
import com.demo.storedemo.R
import com.demo.storedemo.databinding.LayoutAppsItemBinding
import com.demo.storedemo.model.AppInfo
import timber.log.Timber

class AppAdapter(
    private val apps: ArrayList<AppInfo> = arrayListOf(),
    private val itemClick: ((Int, AppInfo) -> Unit)?
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    fun updateList(list: List<AppInfo>) {
        apps.clear()
        apps.addAll(list)
        Timber.d("update ...")
        notifyDataSetChanged()
    }


    fun getApps() = apps

    fun startDownload(pkg: String){
        val target = getAppIndex(pkg)
        apps[target].state = 1
        notifyItemChanged(target)
    }

    fun updateDownloadProgress(pkg: String,progress: Long,total:Long){
        val target = getAppIndex(pkg)
        apps[target].apply {
            this.downloadProgress = progress
            this.downloadTotal = total
        }
        notifyItemChanged(target)
    }

    fun downloadComplete(pkg: String){
        val target = getAppIndex(pkg)
        apps[target].apply {
            downloadProgress = downloadTotal
            state = 2
        }
        notifyItemChanged(target)
    }

    fun installStart(pkg: String?){
        if (pkg.isNullOrEmpty()) return
        val target = getAppIndex(pkg)
        apps[target].apply {
            state = 3
        }
        notifyItemChanged(target)
    }

    fun installResult(pkg: String?,result: Boolean){
        if (pkg.isNullOrEmpty()) return
        val target = getAppIndex(pkg)
        apps[target].apply {
            state = if (result) 4 else -2
            installed = result
        }
        notifyItemChanged(target)
    }


    private fun getAppIndex(pkg: String): Int{
        var index = -1
        for (i in apps.indices) {
            if (apps[i].packageName == pkg) {
                index = i
                break
            }
        }
        return index
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutAppsItemBinding.inflate(inflater, parent, false)
        return AppViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val info = apps.get(position)
        holder.bind(position, info)
    }


    inner class AppViewHolder(private val binding: LayoutAppsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(index: Int, info: AppInfo) {
            //Timber.d("bind info $info")
            binding.root.setOnClickListener {
                itemClick?.invoke(0, info)
            }
            binding.actionBtn.setOnClickListener {
                itemClick?.invoke(1,info)
            }

            if (info.state in 1 .. 3){
                binding.downloadLayout.visibility = View.VISIBLE
                binding.progressBar.progress = info.downloadProgress.toInt()
                binding.progressBar.max = info.downloadTotal.toInt()
                binding.downloadSizeTv.text = when(info.state) {
                    1 -> "${
                        Formatter.formatFileSize(
                            appContext,
                            info.downloadProgress
                        )
                    }/${Formatter.formatFileSize(appContext, info.downloadTotal)}"
                    2 -> "download complete"
                    3 -> "install..."
                    else -> ""
                }

            }else{
                binding.downloadLayout.visibility = View.GONE
            }

            binding.actionBtn.visibility = if(info.installed || info.state in 1..3) View.GONE else View.VISIBLE

            binding.titleTv.text = info.name
            binding.summaryTv.text = "分类:${info.category} 评价:${info.rating} 大小:${Formatter.formatFileSize(appContext,info.size)}"

            if (info.iconUrl.isNotEmpty()) {
                Glide.with(binding.root.context).load(info.iconUrl)
                    .placeholder(R.drawable.ic_launcher_foreground).into(binding.iconIV)
            }
        }
    }


}