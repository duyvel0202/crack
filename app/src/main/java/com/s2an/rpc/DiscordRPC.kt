package com.s2an.rpc

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

object DiscordRPC {
    private var ws: WebSocket? = null
    private var heartbeat: Long = 0
    private var seq: Int? = null
    private var connected = false

    fun connect(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("wss://gateway.discord.gg/?v=9&encoding=json")
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = JSONObject(text)
                val op = json.getInt("op")
                if (json.has("s")) seq = json.getInt("s")
                when (op) {
                    10 -> {
                        heartbeat = json.getJSONObject("d").getLong("heartbeat_interval")
                        startHeartbeat()
                        sendIdentify(token)
                    }
                    0 -> {
                        if (json.optString("t") == "READY") connected = true
                    }
                    9 -> webSocket.close(1000, "invalid session")
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connected = false
            }
        })
    }

    private fun sendIdentify(token: String) {
        val d = JSONObject().apply {
            put("token", token)
            put("properties", JSONObject().apply {
                put("\$os", "Android")
                put("\$browser", "Discord Android")
                put("\$device", "Android")
            })
            put("presence", JSONObject().apply {
                put("status", "online")
                put("activities", JSONArray())
            })
        }
        val payload = JSONObject().apply {
            put("op", 2)
            put("d", d)
        }
        ws?.send(payload.toString())
    }

    private fun startHeartbeat() {
        Thread {
            while (true) {
                Thread.sleep(heartbeat)
                val payload = JSONObject().apply {
                    put("op", 1)
                    put("d", seq ?: JSONObject.NULL)
                }
                ws?.send(payload.toString())
            }
        }.start()
    }

    fun update(appName: String, details: String, state: String, image: String) {
        if (!connected) return
        val activity = JSONObject().apply {
            put("name", appName)
            put("type", 0)
            put("details", details)
            put("state", state)
            put("timestamps", JSONObject().apply {
                put("start", System.currentTimeMillis())
            })
            put("assets", JSONObject().apply {
                put("large_image", image)
                put("large_text", details)
            })
        }
        val payload = JSONObject().apply {
            put("op", 3)
            put("d", JSONObject().apply {
                put("status", "online")
                put("since", 0)
                put("activities", JSONArray().put(activity))
                put("afk", false)
            })
        }
        ws?.send(payload.toString())
    }

    fun disconnect() {
        ws?.close(1000, "user disconnect")
        ws = null
        connected = false
    }
}