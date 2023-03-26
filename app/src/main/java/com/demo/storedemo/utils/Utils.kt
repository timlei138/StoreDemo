package com.demo.storedemo.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import timber.log.Timber


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
