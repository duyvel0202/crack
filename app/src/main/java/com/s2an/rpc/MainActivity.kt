package com.s2an.rpc

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tokenInput = findViewById<EditText>(R.id.tokenInput)
        val appIdInput = findViewById<EditText>(R.id.appIdInput)
        val connectBtn = findViewById<Button>(R.id.connectBtn)
        val disconnectBtn = findViewById<Button>(R.id.disconnectBtn)
        val notifBtn = findViewById<Button>(R.id.notifBtn)
        val usageBtn = findViewById<Button>(R.id.usageBtn)
        val statusText = findViewById<TextView>(R.id.statusText)

        notifBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        usageBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        connectBtn.setOnClickListener {
            val token = tokenInput.text.toString().trim()
            if (token.isNotEmpty()) {
                DiscordRPC.connect(token)
                statusText.text = "Đang kết nối..."
                connectBtn.isEnabled = false
                disconnectBtn.isEnabled = true
            }
        }

        disconnectBtn.setOnClickListener {
            DiscordRPC.disconnect()
            statusText.text = "Đã ngắt kết nối"
            connectBtn.isEnabled = true
            disconnectBtn.isEnabled = false
        }

        disconnectBtn.isEnabled = false
    }
}