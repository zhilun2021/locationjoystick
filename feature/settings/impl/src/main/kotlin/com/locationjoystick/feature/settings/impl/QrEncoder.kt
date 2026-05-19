package com.locationjoystick.feature.settings.impl

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import org.json.JSONObject

/**
 * Encodes chunk data into QR code bitmaps using ZXing library.
 *
 * Each chunk is serialized as JSON with fields:
 * - k: checksum (SHA-256 hash of data)
 * - v: schema version
 * - session: session ID for grouping
 * - chunk: chunk index (0-based)
 * - total: total number of chunks
 * - d: compressed data payload (base64)
 *
 * Output is a 512x512 RGB-565 bitmap suitable for display in Compose.
 *
 * @see QrChunker for chunk generation
 * @see ChunkEnvelope for input format
 */
object QrEncoder {
    /**
     * Encodes a chunk envelope into a QR code bitmap.
     *
     * @param envelope Chunk data to encode
     * @return Bitmap ready for display, or null on failure
     */
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
