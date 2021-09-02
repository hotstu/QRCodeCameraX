package io.github.hotstu.qrcodex.ml

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage


/**
 * recognize qrcode using
 * [google ML kit](https://developers.google.com/ml-kit/vision/barcode-scanning/android)
 * @author ivan200 [link](https://github.com/ivan200)
 * @desc
 * @since 2021-01-08
 */
class MLQRcodeAnalyzer(private val resultHandler: (String?) -> Unit) : ImageAnalysis.Analyzer {
    //private val fpsDelegate = FpsDelegate()

    private val scanner: BarcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    private var pendingTask: Task<out Any>? = null

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            //fpsDelegate.tick()

            pendingTask = scanner.process(image)
                .addOnSuccessListener {
                    resultHandler.invoke(it.joinToString { barcode -> barcode?.rawValue.toString() })
                }
                .addOnFailureListener {
                    resultHandler.invoke(it.message)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
        //Log.d("MLQRcodeAnalyzer", "frames: ${fpsDelegate.framesPerSecond}")
    }
}
