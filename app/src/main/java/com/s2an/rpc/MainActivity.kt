package com.s2an.rpc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tokenInput: EditText
    private lateinit var appIdInput: EditText
    private lateinit var connectBtn: Button
    private lateinit var disconnectBtn: Button
    private lateinit var notifBtn: Button
    private lateinit var usageBtn: Button
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenInput = findViewById(R.id.tokenInput)
        appIdInput = findViewById(R.id.appIdInput)
        connectBtn = findViewById(R.id.connectBtn)
        disconnectBtn = findViewById(R.id.disconnectBtn)
        notifBtn = findViewById(R.id.notifBtn)
        usageBtn = findViewById(R.id.usageBtn)
        statusText = findViewById(R.id.statusText)

        val prefs = getSharedPreferences("rpc_prefs", MODE_PRIVATE)
        tokenInput.setText(prefs.getString("token", ""))
        appIdInput.setText(prefs.getString("appId", ""))

        notifBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        usageBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        connectBtn.setOnClickListener {
            val token = tokenInput.text.toString().trim()
            val appId = appIdInput.text.toString().trim()

            if (token.isEmpty()) {
                statusText.text = "Vui lòng nhập token"
                return@setOnClickListener
            }

            prefs.edit().putString("token", token).putString("appId", appId).apply()

            statusText.text = "Đang kết nối..."
            connectBtn.isEnabled = false
            disconnectBtn.isEnabled = true

            DiscordRPC.connect(token)

            Handler(Looper.getMainLooper()).postDelayed({
                if (DiscordRPC.isConnected()) {
                    statusText.text = "Đã kết nối thành công"
                } else {
                    statusText.text = "Kết nối thất bại - kiểm tra token"
                    connectBtn.isEnabled = true
                    disconnectBtn.isEnabled = false
                }
            }, 5000)
        }

        disconnectBtn.setOnClickListener {
            DiscordRPC.disconnect()
            statusText.text = "Đã ngắt kết nối"
            connectBtn.isEnabled = true
            disconnectBtn.isEnabled = false
        }

        disconnectBtn.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        DiscordRPC.disconnect()
    }
}
