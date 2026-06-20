package com.locationjoystick.feature.settings.impl

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val TAG = "QrShareDialog"

@Composable
fun QrShareDialog(
    chunks: List<ChunkEnvelope>,
    skippedRoutes: List<String>,
    onDismiss: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { chunks.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Encode all bitmaps once and cache them for the lifetime of this dialog.
    val bitmaps =
        remember(chunks) {
            chunks.map { QrEncoder.encodeToQr(it) }
        }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp),
        ) {
            // Warning banner if routes were skipped due to size.
            if (skippedRoutes.isNotEmpty()) {
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "${skippedRoutes.size} route(s) too large for QR:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                        skippedRoutes.forEach { name ->
                            Text(name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // QR pager.
            HorizontalPager(state = pagerState) { page ->
                val bitmap = bitmaps.getOrNull(page)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR chunk ${page + 1}",
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        )
                    } else {
                        Text("Failed to encode QR")
                    }
                }
            }

            // Progress indicator.
            Text(
                "Chunk ${pagerState.currentPage + 1} of ${chunks.size}",
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp),
                style = MaterialTheme.typography.labelSmall,
            )

            // Prev / Share / Next row.
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                ) {
                    Text("← Prev")
                }

                Button(
                    onClick = {
                        val bitmap = bitmaps.getOrNull(pagerState.currentPage)
                        if (bitmap != null) {
                            scope.launch { shareQrBitmap(context, bitmap, pagerState.currentPage + 1) }
                        }
                    },
                ) {
                    Text("Share")
                }

                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    enabled = pagerState.currentPage < chunks.size - 1,
                ) {
                    Text("Next →")
                }
            }

            Button(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
            ) {
                Text("Done")
            }
        }
    }
}

private suspend fun shareQrBitmap(
    context: Context,
    bitmap: Bitmap,
    chunkNumber: Int,
) {
    withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "qr_share").also { it.mkdirs() }
            val file = File(cacheDir, "qr_chunk_$chunkNumber.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(intent, "Share QR Code"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Share QR bitmap failed", e)
        }
    }
}
