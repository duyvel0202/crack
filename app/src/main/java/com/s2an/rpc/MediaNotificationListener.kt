package com.s2an.rpc

import android.app.Notification
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MediaNotificationListener : NotificationListenerService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastUpdate = 0L

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val pkg = sbn.packageName

        val appName = when (pkg) {
            "com.google.android.youtube" -> "YouTube"
            "com.zhiliaoapp.musically" -> "TikTok"
            "com.soundcloud.android" -> "SoundCloud"
            "com.spotify.music" -> "Spotify"
            else -> null
        }

        if (appName != null) {
            val imageKey = when (pkg) {
                "com.google.android.youtube" -> "youtube"
                "com.zhiliaoapp.musically" -> "tiktok"
                "com.soundcloud.android" -> "soundcloud"
                "com.spotify.music" -> "spotify"
                else -> "app"
            }
            DiscordRPC.update(
                appName = appName,
                details = "Đang xem $appName",
                state = title,
                image = imageKey
            )
            lastUpdate = System.currentTimeMillis()
        }
    }

    private fun detectGame(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 1000 * 60 * 5
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            ?: return null
        val top = stats.maxByOrNull { it.lastTimeUsed } ?: return null
        val pkg = top.packageName

        val games = mapOf(
            "com.dts.freefireth" to "Free Fire",
            "com.dts.freefiremax" to "Free Fire MAX",
            "com.garena.game.kgvn" to "Liên Quân Mobile",
            "com.garena.game.kgtw" to "Liên Quân Mobile",
            "com.garena.game.kgid" to "Liên Quân Mobile",
            "com.garena.game.kgth" to "Liên Quân Mobile",
            "com.mojang.minecraftpe" to "Minecraft",
            "com.tencent.ig" to "PUBG Mobile",
            "com.riotgames.league.wildrift" to "Wild Rift",
            "com.garena.game.codm" to "Call of Duty Mobile",
            "com.miHoYo.GenshinImpact" to "Genshin Impact",
            "com.miHoYo.hkrpg" to "Honkai Star Rail",
            "com.roblox.client" to "Roblox",
            "com.supercell.clashofclans" to "Clash of Clans",
            "com.supercell.brawlstars" to "Brawl Stars",
            "com.epicgames.fortnite" to "Fortnite",
            "com.activision.callofduty.shooter" to "COD Mobile",
            "com.ea.gp.fifamobile" to "FIFA Mobile",
            "com.gameloft.android.ANMP.GloftA9HM" to "Asphalt 9",
            "com.netmarble.kof" to "The King of Fighters",
            "com.pubg.newstate" to "PUBG New State",
            "com.tencent.lolm" to "League of Legends: Wild Rift",
            "com.tencent.tmgp.pubgmhd" to "PUBG Mobile",
            "com.netease.lztgglobal" to "LifeAfter",
            "com.netease.dwrg" to "Identity V",
            "com.mobile.legends" to "Mobile Legends",
            "com.moonton.mobilehero" to "Mobile Legends"
        )
        return games[pkg]
    }

    private val ticker = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            if (now - lastUpdate > 10000) {
                val game = detectGame()
                if (game != null) {
                    DiscordRPC.update(
                        appName = "Game",
                        details = "Đang chơi $game",
                        state = "Trong trận",
                        image = "game"
                    )
                    lastUpdate = now
                }
            }
            handler.postDelayed(this, 15000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(ticker)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(ticker)
    }
}