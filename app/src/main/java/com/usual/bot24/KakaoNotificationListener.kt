package com.usual.bot24

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class KakaoNotificationListener : NotificationListenerService() {

    private val client = OkHttpClient()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.kakao.talk") return

        val extras = sbn.notification.extras
        val sender = extras.getString("android.title") ?: return
        val room = extras.getString("android.subText") ?: "unknown"
        val message = extras.getCharSequence("android.text")?.toString() ?: return

        val json = JSONObject().apply {
            put("sender", sender)
            put("room", room)
            put("message", message)
        }

        sendToServer(json)
    }

    private fun sendToServer(json: JSONObject) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://192.168.0.5:5000/kakao")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("KakaoNotification", "전송 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("KakaoNotification", "전송 성공: ${response.body?.string()}")
            }
        })
    }
}
