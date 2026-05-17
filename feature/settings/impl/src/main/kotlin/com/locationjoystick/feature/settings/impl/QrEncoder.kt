package com.locationjoystick.feature.settings.impl

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import org.json.JSONObject

object QrEncoder {
    fun encodeToQr(envelope: ChunkEnvelope): Bitmap? =
        try {
            val json =
                JSONObject()
                    .apply {
                        put("k", envelope.k)
                        put("v", envelope.v)
                        put("session", envelope.session)
                        put("chunk", envelope.chunk)
                        put("total", envelope.total)
                        put("d", envelope.d)
                    }.toString()

            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(json, BarcodeFormat.QR_CODE, 512, 512)
            createBitmapFromBitMatrix(bitMatrix)
        } catch (e: Exception) {
            Log.e(TAG, "QR encode failed", e)
            null
        }

    private fun createBitmapFromBitMatrix(bitMatrix: BitMatrix): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private const val TAG = "QrEncoder"
}
