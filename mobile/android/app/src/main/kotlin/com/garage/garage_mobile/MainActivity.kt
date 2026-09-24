package com.garage.garage_mobile

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class MainActivity : FlutterActivity() {
    private val notificationChannelId = "customer_progress"
    private val accountExtra = "notification_account"
    private var bridge: MethodChannel? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(
                notificationChannelId, "Tiến độ xe", NotificationManager.IMPORTANCE_HIGH
            ))
        }
        bridge = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.garage/notifications")
        bridge!!.setMethodCallHandler { call, result ->
            when (call.method) {
                "show" -> {
                    val id = call.argument<Int>("id")
                    val account = call.argument<String>("account")
                    val title = call.argument<String>("title")
                    val content = call.argument<String>("content")
                    if (id == null || account == null || title == null || content == null) {
                        result.error("invalid_notification", "Missing notification fields", null)
                    } else {
                        if (Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            val tap = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra(accountExtra, account)
                            }
                            val pendingIntent = PendingIntent.getActivity(this, id, tap,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Notification.Builder(this, notificationChannelId)
                            } else {
                                Notification.Builder(this).setPriority(Notification.PRIORITY_HIGH)
                            }
                            manager.notify(id, builder
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(title)
                                .setContentText(content)
                                .setStyle(Notification.BigTextStyle().bigText(content))
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setVisibility(Notification.VISIBILITY_PRIVATE)
                                .build())
                        }
                        result.success(null)
                    }
                }
                "clear" -> {
                    manager.cancelAll()
                    result.success(null)
                }
                "takePendingTap" -> {
                    val account = intent?.getStringExtra(accountExtra)
                    intent?.removeExtra(accountExtra)
                    result.success(account)
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val account = intent.getStringExtra(accountExtra) ?: return
        bridge?.invokeMethod("notificationTap", account)
        intent.removeExtra(accountExtra)
    }
}
