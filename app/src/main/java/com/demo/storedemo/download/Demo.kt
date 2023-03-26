package com.demo.storedemo.download

import android.content.pm.PackageInstaller
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class Demo {
    @Throws(IOException::class)
    private fun runInstallWrite(
        installer: PackageInstaller,
        sessionId: Int,
        splitName: String,
        apkPath: String
    ) {
        val file = File(apkPath)
        val sizeBytes = file.length()
        val session = installer.openSession(sessionId)
        val `in`: InputStream = FileInputStream(apkPath)
        val out = session.openWrite(splitName, 0, sizeBytes)
        val buffer = ByteArray(65536)
        var c: Int
        while (`in`.read(buffer).also { c = it } != -1) {
            out.write(buffer, 0, c)
        }
        session.fsync(out)
        try {
            out.close()
            `in`.close()
            session.close()
        } catch (ignored: IOException) {
        }
    }
}