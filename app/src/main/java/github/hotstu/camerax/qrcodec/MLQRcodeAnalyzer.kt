package github.hotstu.camerax.qrcodec

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata


/**
 * recognize qrcode using google ML kit
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 6/10/19
 */
class MLQRcodeAnalyzer : ImageAnalysis.Analyzer, OnSuccessListener<List<FirebaseVisionBarcode>>, OnFailureListener {

    private val detector: FirebaseVisionBarcodeDetector by lazy {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_QR_CODE
            )
            .build()
        FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

    var pendingTask: Task<out Any>? = null


    override fun analyze(image: ImageProxy) {
        // Throttle calls to the detector.
        if (pendingTask != null && !pendingTask!!.isComplete) {
            Log.d("MLQRcodeAnalyzer", "Throttle calls to the detector")
            image.close()
            return
        }
        val rotationDegrees = image.imageInfo.rotationDegrees
        //YUV_420 is normally the input type here
        var rotation = rotationDegrees % 360
        if (rotation < 0) {
            rotation += 360
        }
        val mediaImage = FirebaseVisionImage.fromMediaImage(image.image!!, when (rotation) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> {
                Log.e("MLQRcodeAnalyzer", "unexpected rotation: $rotationDegrees")
                FirebaseVisionImageMetadata.ROTATION_0
            }
        })
        pendingTask = detector.detectInImage(mediaImage).also {
            it.addOnSuccessListener(this)
            it.addOnFailureListener(this)
        }
        image.close()
    }

    override fun onFailure(p0: Exception) {
        Log.d("MLQRcodeAnalyzer", "onFailure:$p0")
    }

    override fun onSuccess(result: List<FirebaseVisionBarcode>) {
        for (barcode in result) {
            Log.d("MLQRcodeAnalyzer", "onSuccess:${barcode.rawValue}")
        }

    }
}
