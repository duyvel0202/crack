package com.s2an.rpc

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object DiscordRPC {
    private var ws: WebSocket? = null
    private var heartbeat: Long = 0
    private var seq: Int? = null
    private var connected = false
    private var token: String = ""

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun connect(tokenInput: String) {
        token = tokenInput
        val request = Request.Builder()
            .url("wss://gateway.discord.gg/?v=9&encoding=json")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .addHeader("Origin", "https://discord.com")
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val op = json.getInt("op")
                    if (json.has("s") && !json.isNull("s")) seq = json.getInt("s")

                    when (op) {
                        10 -> {
                            heartbeat = json.getJSONObject("d").getLong("heartbeat_interval")
                            startHeartbeat()
                            sendIdentify()
                        }
                        11 -> {
                        }
                        0 -> {
                            val t = json.optString("t")
                            if (t == "READY") connected = true
                        }
                        9 -> {
                            connected = false
                            webSocket.close(1000, "invalid session")
                        }
                        7 -> {
                            connected = false
                            webSocket.close(1000, "reconnect")
                        }
                    }
                } catch (e: Exception) {
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connected = false
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                connected = false
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                connected = false
            }
        })
    }

    private fun sendIdentify() {
        val d = JSONObject().apply {
            put("token", token)
            put("capabilities", 125)
            put("properties", JSONObject().apply {
                put("\$os", "Windows")
                put("\$browser", "Chrome")
                put("\$device", "")
            })
            put("presence", JSONObject().apply {
                put("status", "online")
                put("since", 0)
                put("activities", JSONArray())
                put("afk", false)
            })
            put("compress", false)
        }
        val payload = JSONObject().apply {
            put("op", 2)
            put("d", d)
        }
        ws?.send(payload.toString())
    }

    private fun startHeartbeat() {
        Thread {
            while (connected || ws != null) {
                try {
                    Thread.sleep(heartbeat)
                    val payload = JSONObject().apply {
                        put("op", 1)
                        put("d", seq ?: JSONObject.NULL)
                    }
                    ws?.send(payload.toString())
                } catch (e: Exception) {
                    break
                }
            }
        }.start()
    }

    fun update(appName: String, details: String, state: String, image: String) {
        if (!connected || ws == null) return
        try {
            val activity = JSONObject().apply {
                put("name", appName)
                put("type", 0)
                put("details", details)
                put("state", state)
                put("timestamps", JSONObject().apply {
                    put("start", System.currentTimeMillis())
                })
                if (image.isNotEmpty()) {
                    put("assets", JSONObject().apply {
                        put("large_image", image)
                        put("large_text", details)
                    })
                }
                put("application_id", appName)
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
        } catch (e: Exception) {
        }
    }

    fun isConnected(): Boolean = connected

    fun disconnect() {
        try {
            ws?.close(1000, "user disconnect")
        } catch (e: Exception) {
        }
        ws = null
        connected = false
    }
}
