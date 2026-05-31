package com.locationjoystick.feature.settings.impl

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.json.JSONObject

/**
 * CameraX ImageAnalysis analyzer that decodes QR codes using ZXing.
 *
 * Used by [QrScannerScreen] to scan QR codes emitted by [QrShareDialog].
 * Each scanned QR is parsed as JSON and converted to a [ChunkEnvelope].
 *
 * Key features:
 * - Debouncing: ignores duplicate scans within 500ms
 * - YUV format handling: converts camera YUV_420_888 to ZXing's planar format
 * - Error tolerance: logs failures but doesn't crash on bad codes
 *
 * @param onQrScanned Callback invoked with successfully parsed [ChunkEnvelope]
 */
class ZxingImageAnalyzer(
    private val onQrScanned: (ChunkEnvelope) -> Unit,
) : ImageAnalysis.Analyzer {
    private val multiFormatReader =
        MultiFormatReader().apply {
            setHints(
                mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                    DecodeHintType.TRY_HARDER to true,
                ),
            )
        }
    private var lastScanTime = 0L
    private var lastScannedData = ""

    override fun analyze(image: ImageProxy) {
        try {
            if (image.format != ImageFormat.YUV_420_888) return

            val yPlane = image.planes[0]
            val yBuffer = yPlane.buffer
            val yStride = yPlane.rowStride
            // PlanarYUVLuminanceSource requires dataWidth * dataHeight bytes.
            // yBuffer.remaining() is typically rowStride*(height-1)+width (last row has no padding),
            // so allocate the full rowStride*height and fill what the buffer provides.
            val yBytes = ByteArray(yStride * image.height)
            yBuffer.get(yBytes, 0, minOf(yBytes.size, yBuffer.remaining()))

            val source =
                PlanarYUVLuminanceSource(
                    yBytes,
                    yStride,
                    image.height,
                    0,
                    0,
                    image.width,
                    image.height,
                    false,
                )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val rawResult = multiFormatReader.decodeWithState(binaryBitmap)
            val json = rawResult.text
            Log.d(TAG, "QR decoded: ${json.take(80)}")

            // Debounce: skip duplicate scans within 1 second
            val now = System.currentTimeMillis()
            if (json == lastScannedData && now - lastScanTime < 1000L) {
                return
            }

            val envelope = parseEnvelope(json)
            lastScannedData = json
            lastScanTime = now
            Log.d(TAG, "chunk ${envelope.chunk}/${envelope.total} session=${envelope.session}")
            onQrScanned(envelope)
        } catch (e: NotFoundException) {
            // No QR found — silent, keep scanning
        } catch (e: Exception) {
            Log.w(TAG, "Scan error", e)
        } finally {
            image.close()
        }
    }

    private fun parseEnvelope(json: String): ChunkEnvelope {
        val obj = JSONObject(json)
        return ChunkEnvelope(
            k = obj.getString("k"),
            v = obj.getInt("v"),
            session = obj.getString("session"),
            chunk = obj.getInt("chunk"),
            total = obj.getInt("total"),
            d = obj.getString("d"),
        )
    }

    private companion object {
        const val TAG = "ZxingImageAnalyzer"
    }
}
