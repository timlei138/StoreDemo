package com.demo.storedemo.ui.home

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.storedemo.App.Companion.appContext
import com.demo.storedemo.R
import com.demo.storedemo.model.AppInfo
import timber.log.Timber

class AppRankAdapter(val apps: ArrayList<AppInfo>) : RecyclerView.Adapter<AppRankAdapter.RankViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_rank_list_item,parent,false)
        return RankViewHolder(view)
    }

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        holder.bind(position,apps[position])
    }


    inner class RankViewHolder(val view: View) : RecyclerView.ViewHolder(view){

        val indexTv = view.findViewById<TextView>(R.id.indexTv)
        val iconImgIv = view.findViewById<ImageView>(R.id.appIconIv)
        val appNameTv = view.findViewById<TextView>(R.id.appNameTv)
        val starTv = view.findViewById<TextView>(R.id.starTv)
        val descTv = view.findViewById<TextView>(R.id.appDesc)
        val installBtn = view.findViewById<Button>(R.id.btnInstall)

        fun bind(index: Int,info: AppInfo){
            Timber.d("info=> $info")
            with(info){
                indexTv.text = (index + 1).toString()
                appNameTv.text = this.name
                starTv.text = "$rating ${getFileSize(info.size)}"
                descTv.text = description
                Glide.with(view.context).load(iconUrl).into(iconImgIv)
            }
        }

        private fun getFileSize(size: Long): String{
            return Formatter.formatFileSize(appContext,size)
        }
    }

}