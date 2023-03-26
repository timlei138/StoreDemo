package com.demo.storedemo.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.storedemo.data.Result
import com.demo.storedemo.data.StoreRepository
import com.demo.storedemo.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: StoreRepository) : ViewModel() {

    val observableApps = MutableLiveData<List<AppInfo>?>()

    fun fetchApps(){
        viewModelScope.launch {
            val result = repository.getApps()

            when(result){
                is Result.Success -> {
                    observableApps.value = result.data
//                    val data = arrayListOf<AppInfo>()
//                    result.data.forEach {
//                        if (isZipFiles(it.downloadUrl)){
//                            data.add(it)
//                        }
//                    }

                }
                is Result.Error -> observableApps.value = emptyList()
                else -> observableApps.value = emptyList()
            }
        }
    }

    private fun isZipFiles(url: String): Boolean{
        return url.contains(".zip")
    }



}