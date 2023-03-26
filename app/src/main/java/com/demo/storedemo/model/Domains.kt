package com.demo.storedemo.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.versionedparcelable.ParcelField
import kotlinx.parcelize.Parcelize


data class ListJsonResult(
    val data: List<AppInfo>,
    val message: String,
    val status: Int
)


@Entity
@Parcelize
data class AppInfo (
    val category: String,
    val country: String,
    val createDate: String,
    val description: String,
    val developer: String,
    val downloadUrl: String,
    val fileName: String,
    val iconName: String,
    val iconUrl: String,
    val install: String,
    val keywords: String,
    val name: String,
    val packageName: String,
    val price: String,
    val rating: String,
    val releaseDate: String,
    val size: Long = 0,
    val supportAndroidVersion: String,
    val updateDate: String,
    val version: String,
    val videoUrl: String,
    var installed: Boolean = false,
    var downloadProgress: Long = 0,
    var downloadTotal: Long = 0,
    var state: Int = 0
): Parcelable{
    override fun toString(): String {
        return "AppInfo(category='$category', country='$country', createDate=$createDate, description=$description, developer=$developer, downloadUrl='$downloadUrl', fileName='$fileName', iconName='$iconName', iconUrl='$iconUrl', install=$install, keywords=$keywords, name='$name', packageName='$packageName', price=$price, rating=$rating, releaseDate=$releaseDate, size=$size, supportAndroidVersion=$supportAndroidVersion, updateDate=$updateDate, version=$version, videoUrl=$videoUrl, installed=$installed, downloadProgress=$downloadProgress, downloadTotal=$downloadTotal, state=$state)"
    }
}