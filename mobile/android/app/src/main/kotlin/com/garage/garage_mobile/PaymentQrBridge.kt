package com.garage.garage_mobile

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

/** Saves the backend QR to the gallery before the user opens their banking app. */
class PaymentQrBridge(private val activity: Activity, messenger: BinaryMessenger) {
    init {
        MethodChannel(messenger, "com.garage/payment_qr").setMethodCallHandler { call, result ->
            when (call.method) {
                "needsStoragePermission" -> result.success(Build.VERSION.SDK_INT < 29)
                "saveQr" -> {
                    val bytes = call.argument<ByteArray>("bytes")
                    val invoiceId = call.argument<Number>("invoiceId")?.toLong()
                    if (bytes == null || bytes.isEmpty() || bytes.size > 5 * 1024 * 1024 || invoiceId == null || invoiceId <= 0) {
                        result.error("invalid_qr", "Invalid image", null)
                    } else if (Build.VERSION.SDK_INT < 29 && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        result.error("permission_denied", "Storage permission required", null)
                    } else {
                        Thread {
                            try {
                                save(bytes, invoiceId)
                                activity.runOnUiThread { result.success(null) }
                            } catch (_: Exception) {
                                activity.runOnUiThread { result.error("save_failed", "Could not save QR", null) }
                            }
                        }.start()
                    }
                }
                "openMbBank" -> {
                    try {
                        val intent = activity.packageManager.getLaunchIntentForPackage("com.mbmobile")
                        if (intent == null) result.success(false)
                        else {
                            activity.startActivity(intent)
                            result.success(true)
                        }
                    } catch (_: Exception) {
                        result.success(false)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun save(bytes: ByteArray, invoiceId: Long) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth in 1..4096 && bounds.outHeight in 1..4096)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalArgumentException("Invalid QR image")
        val resolver = activity.contentResolver
        var uri: android.net.Uri? = null
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "AutoCare_QR_${invoiceId}_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AutoCare")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val savedUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Cannot create image")
            uri = savedUri
            resolver.openOutputStream(savedUri)?.use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
            } ?: throw IllegalStateException("Cannot write image")
            if (Build.VERSION.SDK_INT >= 29) {
                check(resolver.update(savedUri, ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null) > 0)
            }
        } catch (error: Exception) {
            // Roll back only the incomplete media entry created by this operation.
            uri?.let { runCatching { resolver.delete(it, null, null) } }
            throw error
        } finally {
            bitmap.recycle()
        }
    }
}
