package ro.alexmamo.firebasesigninwithemailandpassword

import android.app.Application
import io.getstream.chat.android.client.ChatClient

class SrteamChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeStreamChat()
    }

    private fun initializeStreamChat() {
        val client = ChatClient.Builder(
            apiKey = "yj2prjbtfw2k",
            appContext = this
        ).build()

        streamChatClient = client
    }

    companion object {
        var streamChatClient: ChatClient? = null
    }
}