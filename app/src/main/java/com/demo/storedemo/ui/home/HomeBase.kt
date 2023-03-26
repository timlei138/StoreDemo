package com.demo.storedemo.ui.home


import androidx.fragment.app.Fragment
import com.demo.storedemo.model.AppInfo
import timber.log.Timber

open class HomeBase : Fragment() {

    private val installList = hashMapOf<String,Int>()

    var parentFragment: HomeFragment? = null
    var installCb: InstallAppCb? = null
    private set

    fun setInstallCb(installAppCb: InstallAppCb){
        installCb = installAppCb
    }

    fun installApk(appInfo: AppInfo,position: Int){
        Timber.d("installApk position $position")
        installList.put(appInfo.packageName,position)
        parentFragment?.installApk(appInfo)

    }

    fun getPositionForPkg(pkg: String): Int{
        Timber.d("getPositionForPkg pkg $pkg ,total ${installList.size}")
        return installList.get(pkg) ?: -1
    }

}


interface InstallAppCb{
    fun downloadStart(pkg: String){}
    fun downloadProgress(pkg: String,progress: Long,total: Long){}
    fun downloadCompleted(pkg: String,result: Boolean,savePath: String? = ""){}
    fun installStart(pkg: String?){}
    fun installCompleted(pkg: String?,result: Boolean){}
}