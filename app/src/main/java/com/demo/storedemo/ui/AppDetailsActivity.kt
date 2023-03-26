package com.demo.storedemo.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Build
import android.os.Bundle
import com.demo.storedemo.databinding.ActivityAppDetailsBinding
import com.demo.storedemo.model.AppInfo
import com.demo.storedemo.utils.readTmpDetailsFile
import timber.log.Timber


class AppDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("onCreate")
        binding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent")
        val tmp = intent?.getParcelableExtra<AppInfo>("app") ?: null
        Timber.d("tmp path $tmp")
        if (tmp == null) finish()

        parseInfo(tmp!!)

    }


    private fun parseInfo(app: AppInfo){

    }
}