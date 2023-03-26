package com.demo.storedemo

import android.app.Application
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import kotlin.properties.Delegates

@HiltAndroidApp
class App : Application() {

    companion object{

        var appContext by Delegates.notNull<Application>()
        private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this


        val glideBuild = GlideBuilder().also {
            it.setDiskCache(InternalCacheDiskCacheFactory(this,"icons",1024 * 1024 * 50))
        }

        Glide.init(this,glideBuild)

        Timber.plant(Timber.DebugTree())
    }


}