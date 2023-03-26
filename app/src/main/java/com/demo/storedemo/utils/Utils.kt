package com.demo.storedemo.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.demo.storedemo.App.Companion.appContext
import com.demo.storedemo.model.AppInfo
import okhttp3.internal.closeQuietly
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


fun createTmpDetailsFile(app: AppInfo): String{
    val tmp = File(appContext.cacheDir,app.packageName.plus(".tmp"))
    val fos = FileOutputStream(tmp)
    val oos = ObjectOutputStream(fos)
    oos.writeObject(app)
    oos.closeQuietly()
    fos.closeQuietly()
    return tmp.absolutePath

}

fun readTmpDetailsFile(path: String): AppInfo{
    val tmp = File(path)
    val fis = FileInputStream(tmp)
    val ois = ObjectInputStream(fis)
    val info = ois.readObject() as AppInfo
    ois.closeQuietly()
    fis.closeQuietly()
    return info
}

fun openApp(context: Context,pkg: String){
    try {
        context.startActivity(Intent(Intent.ACTION_MAIN).apply {
            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK)
            component = context.packageManager.getLaunchIntentForPackage(pkg)?.component
        })
    }catch (e: ActivityNotFoundException){
        e.printStackTrace()
        Timber.d("start App $pkg failed")
    }
}
