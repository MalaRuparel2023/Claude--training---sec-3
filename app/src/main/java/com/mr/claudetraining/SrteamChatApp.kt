package com.mr.claudetraining

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.mr.claudetraining.data.sync.SyncManager
import javax.inject.Inject

@HiltAndroidApp
class SrteamChatApp : Application() {

    @Inject lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        syncManager.start()
    }
}