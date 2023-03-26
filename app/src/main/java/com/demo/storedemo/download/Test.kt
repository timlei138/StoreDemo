package com.demo.storedemo.download

import com.demo.storedemo.model.AppInfo

class Test {
    fun find(list: List<AppInfo>) {
        val pkg = ""
        var index = -1
        for (i in list.indices) {
            if (list[i].packageName == pkg) {
                index = i
                break
            }
        }
        val order = index + 1
    }
}