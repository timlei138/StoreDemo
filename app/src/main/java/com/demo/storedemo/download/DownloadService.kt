package com.demo.storedemo.download

import android.app.DownloadManager
import android.app.DownloadManager.Request
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.Session
import android.content.pm.PackageInstaller.SessionCallback
import android.content.pm.PackageInstaller.SessionParams
import android.net.Uri
import android.os.*
import androidx.collection.arrayMapOf
import okhttp3.internal.closeQuietly
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipFile

class DownloadService : Service() {


    private val workThread = HandlerThread("downloadThread")

    private lateinit var workHandler: DownloadHandler

    private var dlManager: DownloadManager? = null

    private val MSG_COMPLETED = 1;

    private val MSG_QUERY_PROGRESS = 2;

    private val downloadBinder = DownBinder()

    private val downloadList = arrayMapOf<Long,DownloadInfo>()
    private val installList = arrayMapOf<Int,String>()

    private var downloadCallback: DownloadCallback? = null

    override fun onCreate() {
        Timber.d("onCreate")
        super.onCreate()
        workThread.start()
        registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        workHandler = DownloadHandler(workThread.looper)
        applicationContext.packageManager.packageInstaller.registerSessionCallback(installSessionCb)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: ""
        val apkName = intent?.getStringExtra("name") ?: "${System.currentTimeMillis()}.zip"
        val pkg = intent?.getStringExtra("pkg") ?: ""
        if (url.isNotEmpty() && pkg.isNotEmpty()){
            val downloadInfo = DownloadInfo(pkg,apkName,url)
            download(downloadInfo)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent) = downloadBinder


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadCompleteReceiver)
        workHandler.removeMessages(MSG_COMPLETED)
        workHandler.removeMessages(MSG_QUERY_PROGRESS)
        workThread.quitSafely()
        applicationContext.packageManager.packageInstaller.unregisterSessionCallback(installSessionCb)
    }

    private fun download(info: DownloadInfo){
        val dmManager = getDownloadManager()
        val request = DownloadManager.Request(Uri.parse(info.url)).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or Request.NETWORK_WIFI)
            setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setTitle(info.name)
            setDescription("正在下载${info.name}")
            val savePath = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),"apk_cache").apply {
                if (!exists()){
                    mkdirs()
                }
            }
            val fileName =
            setDestinationInExternalFilesDir(applicationContext,Environment.DIRECTORY_DOWNLOADS,"apk_cache${File.separator}${info.pkg}.zip")
            val path = File(savePath,"${info.pkg}.zip").absolutePath
            info.savePath = path
        }
        val downloadId = dmManager.enqueue(request)
        info.downloadId = downloadId
        downloadList.put(downloadId,info)
        workHandler.sendEmptyMessage(MSG_QUERY_PROGRESS)
        downloadCallback?.downloadStart(info.pkg,info.downloadId)
        Timber.d("download info $info ${downloadList.size}")
    }


    fun setDownloadCallback(cb: DownloadCallback){
        downloadCallback = cb
    }

    fun startDownload(pkg: String,apkName: String,url: String){
        val downloadInfo = DownloadInfo(pkg,apkName,url)
        download(downloadInfo)

    }


    private fun getDownloadManager(): DownloadManager{
        if (dlManager == null)
            dlManager= applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        return dlManager!!
    }


    private val downloadCompleteReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.d("action ${intent?.action}")
            when(intent?.action){
                DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                    val dlId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,0) ?: 0
                    if (dlId >= 0){
                        val bundle = Bundle().apply {
                            putLong("id",dlId)
                        }
                        workHandler.sendMessage(workHandler.obtainMessage(MSG_COMPLETED).apply {
                            data = bundle
                        })
                    }
                }

                "com.demo.storedemo.APK_INSTALL_COMPLETED" ->{

                }
            }
        }
    }


    private val installSessionCb = object : SessionCallback(){
        override fun onCreated(sessionId: Int) {
            Timber.d("onCreated $sessionId")
            downloadCallback?.installStart(installList.get(sessionId))
        }

        override fun onBadgingChanged(sessionId: Int) {
            Timber.d("onBadgingChanged $sessionId")
        }

        override fun onActiveChanged(sessionId: Int, active: Boolean) {
            Timber.d("onActiveChanged $sessionId ,$active")
        }

        override fun onProgressChanged(sessionId: Int, progress: Float) {
            Timber.d("onProgressChanged $sessionId ,$progress")
        }

        override fun onFinished(sessionId: Int, success: Boolean) {
            Timber.d("onFinished $sessionId ,$success")
            downloadCallback?.installResult(installList.get(sessionId),success)
        }

    }


    inner class DownloadHandler(private val looper: Looper) : Handler(looper){

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Timber.d("handler msg ${msg.what}")
            when(msg.what){
                MSG_QUERY_PROGRESS -> {
                    removeMessages(MSG_QUERY_PROGRESS)
                    val query = DownloadManager.Query()
                    val queryArgs = arrayListOf<Long>()
                    downloadList.forEach {
                        queryArgs.add(it.key)
                    }
                    val args = LongArray(queryArgs.size)
                    queryArgs.forEachIndexed { index, l -> args[index] = l }
                    query.setFilterById(*args)
                    val dm = getDownloadManager()
                    val cursor = dm.query(query)
                    Timber.d("args ${args.size} ,size ${cursor.count}")
                    while (cursor.moveToNext()){
                        val downloadId = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                        val info = downloadList.get(downloadId) ?: continue
                        val state = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        Timber.d("downloadId $downloadId state $state")
                        if (state == DownloadManager.STATUS_SUCCESSFUL) {
                            // 成功后取消监听
                            Timber.d("id($downloadId) download complete")
                            downloadCallback?.downloadComplete(info.pkg,info.savePath)
                            downloadList.keys.findLast { it == downloadId }?.apply {
                                val info = downloadList.get(this)
                                downloadList.remove(downloadId)
                                beginInstallApp(info!!.pkg,info?.savePath)
                            }

                        }else if (state == DownloadManager.STATUS_RUNNING){
                            val downloadSize = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            val totalSize = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                            Timber.d("download $downloadSize $totalSize")
                            downloadCallback?.downloadProgress(info.pkg,downloadSize.toLong(),totalSize.toLong())
                        }
                    }
                    cursor.closeQuietly()
                    if (downloadList.size > 0)
                        sendEmptyMessageDelayed(MSG_QUERY_PROGRESS,5000)
                }

                MSG_COMPLETED ->{
                    val id = msg.data.getLong("id")
                    checkDownloadState(id)
                }
                else -> {

                }
            }
        }


        private fun checkDownloadState(id: Long){
            val query = DownloadManager.Query()
            query.setFilterById(id)
            val dmManager = getDownloadManager()
            val cursor = dmManager.query(query)
            if (cursor.moveToFirst()){
                val downloadId = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                val info = downloadList.get(downloadId)
                val state = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (state == DownloadManager.STATUS_SUCCESSFUL && info != null){
                    downloadCallback?.downloadComplete(info.pkg,info.savePath)
                    downloadList.remove(info.downloadId)
                    beginInstallApp(info!!.pkg,info?.savePath)
                }
                Timber.d("$id state is $state")
            }
        }

    }

    inner class DownBinder : Binder(){
        fun getService() = this@DownloadService
    }



    private fun beginInstallApp(pkg: String,apkPath: String?){

        if (apkPath.isNullOrEmpty()){
            Timber.d("download apk path is null")
            return
        }
        val file = File(apkPath)
        if (!file.exists() || !file.name.endsWith("zip")){
            Timber.d("download apk path is invalid")
            return
        }
        val tmpPath = File(file.parentFile.absolutePath,file.name.substring(0,file.name.indexOf(".")))
        tmpPath.mkdirs()

        val splitFiles = ZipFile(file)
        try {
            val splits = splitFiles.entries()
            val bytes = ByteArray(1024 * 1024)
            while (splits.hasMoreElements()){
                val ze = splits.nextElement()
                if (ze.isDirectory){
                    continue
                }
                var fos: FileOutputStream? = null
                var input: InputStream? = null
                Timber.d("unzip ${ze.name}")
                fos = FileOutputStream(File(tmpPath,ze.name))
                input = splitFiles.getInputStream(ze)
                var read = 0
                while (input.read(bytes).also { read = it } != -1){
                    fos.write(bytes,0,read)
                }
                fos.flush()
                fos?.closeQuietly()
                input?.closeQuietly()
            }
            splitFiles.closeQuietly()
            Timber.d("unzip file finish")

            tmpPath.listFiles()?.apply {
                installSplitApk(pkg,this)
            }

        }catch (e: Exception){
            Timber.d("unzip file failed")
            e.printStackTrace()
        }

    }


    private fun installSplitApk(pkg: String,apkPath: Array<File>){
        Timber.d("install $pkg")
        val apkFiles = hashMapOf<String,String>()
        var totalFileSize = 0L
        apkPath.forEach {
            totalFileSize += it.length()
            apkFiles.put(it.name,it.absolutePath)
        }

        val pmInstaller = applicationContext.packageManager.packageInstaller


        val sessionParams = makeInstallParams(pkg,totalFileSize)

        val sessionId = runInstallCreate(pmInstaller,sessionParams)

        installList.put(sessionId,pkg)

        apkFiles.forEach {
            runInstallWrite(pmInstaller,sessionId,it.key,it.value)
        }

        doCommitSession(pmInstaller,sessionId)
    }



    private fun doCommitSession(installer: PackageInstaller,sessionId: Int){
        var session: Session? = null
        try {
            session = installer.openSession(sessionId)
            val pending = Intent("com.demo.storedemo.APK_INSTALL_COMPLETED").let {
                PendingIntent.getBroadcast(this,0,it,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
            session.commit(pending.intentSender)
            session.closeQuietly()
        }catch (e: Exception){
            Timber.d("doCommitSession failed")
            e.printStackTrace()
        }finally {
            session?.closeQuietly()
        }
    }

    private fun runInstallWrite(packageInstaller: PackageInstaller,sessionId:Int,splitName: String,apkPath: String){
        val file = File(apkPath)
        val sizeByte = file.length()
        val session = packageInstaller.openSession(sessionId)
        var input: InputStream? = null
        var output: OutputStream? = null
        val bytes = ByteArray(1024 * 1024 * 5)
        try {
            input = FileInputStream(file)
            output = session.openWrite(splitName,0,sizeByte)
            var c: Int
            while (input.read(bytes).also { c = it } != -1){
                output.write(bytes,0,c)
            }
            session.fsync(output)

        }catch (e: Exception){
            Timber.d("runInstallWrite ${splitName} failed")
            e.printStackTrace()
        }finally {
            output?.closeQuietly()
            input?.closeQuietly()
            session.closeQuietly()
        }
    }

    private fun runInstallCreate(installer: PackageInstaller,params: SessionParams): Int{
        return installer.createSession(params)
    }


    private fun makeInstallParams(pkg: String?,totalSize: Long): SessionParams{
        var sessionParams: SessionParams
        if (pkg.isNullOrEmpty() || !isAppExists(pkg)){
            sessionParams = SessionParams(SessionParams.MODE_FULL_INSTALL)
        }else{
            sessionParams = SessionParams(SessionParams.MODE_INHERIT_EXISTING)
            sessionParams.setAppPackageName(pkg)
        }
        sessionParams.setSize(totalSize)
        return sessionParams
    }


    private fun isAppExists(pkg: String): Boolean{
        val packages = packageManager.getInstalledPackages(0)
        for (app in packages){
            if (pkg in app.packageName) return true
        }
        return false
    }

}


interface DownloadCallback {
    fun downloadStart(pkg: String,downloadId: Long)
    fun downloadProgress(pkg: String,progress: Long,total: Long)
    fun downloadComplete(pkg: String,savePath: String)
    fun installStart(pkg: String?)
    fun installResult(pkg: String?,success: Boolean)
}

data class DownloadInfo(val pkg: String,val name: String ,val url: String,var progress: Long = 0, var total: Long = 0,var downloadId: Long = 0,var savePath: String = ""){
    override fun toString(): String {
        return "${pkg}, $name, $url, $savePath, $progress, $total, $downloadId"
    }
}