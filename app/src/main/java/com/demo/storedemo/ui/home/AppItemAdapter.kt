package com.demo.storedemo.ui.home

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.storedemo.R
import com.demo.storedemo.model.AppInfo
import timber.log.Timber
import java.text.NumberFormat

data class CategoryInfo(val index: Int,val category: String,var apps: ArrayList<AppInfo>)

class AppItemAdapter(val apps: List<CategoryInfo>,val layoutManager: LinearLayoutManager,val canInstall: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var onItemClick: ((Int,AppInfo,Int)->Unit)? = null

    fun downloadStart(position: Int,pkg: String){
        Timber.d("downloadStart  $pkg $position")
        val index = findTargetItemPosition(position,pkg)
        if (index != -1){
            getTargetItem(position, index)?.text = "0%"
            updateState(position,index,1)
        }

    }

    fun downloadProgress(position: Int,pkg: String,progress: Long,total: Long){
        Timber.d("downloadProgress  $pkg $position")
        val index = findTargetItemPosition(position,pkg)
        if (index != -1){
            val percent = NumberFormat.getPercentInstance().format(progress/total.toFloat())
            getTargetItem(position, index)?.text = percent
            updateState(position,index,1,progress,total)
        }
    }

    fun downloadCompleted(position: Int,pkg: String,result: Boolean){
        Timber.d("downloadCompleted  $pkg $position")
        val index = findTargetItemPosition(position,pkg)
        if (index != -1){
            getTargetItem(position, index)?.text = "下载完成"
            updateState(position,index,1)
        }
    }


    fun installStart(position: Int,pkg: String){
        Timber.d("installStart  $pkg $position")
        val index = findTargetItemPosition(position,pkg)
        if (index != -1){
            getTargetItem(position, index)?.text = "安装中"
            updateState(position,index,1)
        }
    }

    fun installCompleted(position: Int,pkg: String,result: Boolean){
        Timber.d("installCompleted  $pkg $position")
        val index = findTargetItemPosition(position,pkg)
        if (index != -1){
            getTargetItem(position, index)?.apply {
                visibility = if (result) View.INVISIBLE else View.INVISIBLE
                text = if (!result) "安装失败" else ""
            }
            updateState(position,index,0, installed = result)
        }
    }


    private fun findTargetItemPosition(position: Int,pkg: String): Int{
        Timber.d("findTargetItemPosition ${(position - 1) / 2}")
        val list = apps.get((position - 1) / 2).apps
        for (i in list.indices){
            if (list[i].packageName == pkg)
                return i
        }
        return -1
    }

    private fun getTargetItem(position: Int,index: Int) : TextView?{
        val rootView = layoutManager.findViewByPosition(position)?.findViewById<LinearLayout>(R.id.horizationAppLayout)
        return rootView?.getChildAt(index)?.findViewById<TextView>(R.id.btnInstall)
    }

    private fun updateState(position: Int,index: Int,st: Int,pro: Long = 0L,total: Long = 0L,installed: Boolean = false){
        apps.get((position - 1) / 2).apps[index].apply {
            this.state = st
            this.downloadProgress = pro
            this.downloadTotal = total
            this.installed = installed
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_apps_list_label,parent,false)
            TitleViewHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_apps_list_content,parent,false)
            ContentViewHolder(view)
        }
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is TitleViewHolder -> {
                val label = apps.get(position / 2).category
                Timber.d("label $label, ${label.isNotEmpty()}")
                holder.bind(label)
            }

            is ContentViewHolder -> {
                val apps = apps.get((position - 1) / 2 ).apps
                holder.bind(position,apps)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return position % 2
    }


    override fun getItemCount() = apps.size * 2

    internal inner class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bind(label: String){
            val labelTv = itemView.findViewById<TextView>(R.id.categoryLabel)
            labelTv.text = label
        }
    }


    internal inner class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val appLayout = itemView.findViewById<LinearLayout>(R.id.horizationAppLayout)

        fun bind(position: Int,apps: ArrayList<AppInfo>){

            val showApps = if (apps.size > 20) apps.subList(0,20) else apps

            appLayout.removeAllViews()

            showApps.forEach { app ->
                val itemView = LayoutInflater.from(itemView.context).inflate(R.layout.layout_horization_app_item,null,false)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                params.gravity = Gravity.CENTER
                params.setMargins(30,10,30,10)
                itemView.findViewById<TextView>(R.id.appName).text = app.name
                val icon = itemView.findViewById<ImageView>(R.id.appIcon)
                Glide.with(itemView.context).load(app.iconUrl).into(icon)


                val installBtn = itemView.findViewById<TextView>(R.id.btnInstall)
                installBtn.visibility = if (canInstall && !app.installed) View.VISIBLE else View.INVISIBLE
                installBtn.setOnClickListener {
                    Timber.d("app state ${app.state}")
                    if (app.state == 0)
                        onItemClick?.invoke(position,app,0)
                }

                itemView.setOnClickListener {
                    onItemClick?.invoke(position,app,1)
                }

                appLayout.addView(itemView,params)
            }
        }
    }
}