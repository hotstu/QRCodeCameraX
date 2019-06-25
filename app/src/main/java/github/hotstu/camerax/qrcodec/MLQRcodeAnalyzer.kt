package github.hotstu.camerax.qrcodec

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage


/**
 * recognize qrcode using google ML kit
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 6/10/19
 */
class MLQRcodeAnalyzer : ImageAnalysis.Analyzer, OnSuccessListener<List<FirebaseVisionBarcode>>, OnFailureListener {

    private val detector: FirebaseVisionBarcodeDetector by lazy {
        FirebaseVision.getInstance().visionBarcodeDetector
    }


    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        //YUV_420 is normally the input type here
        val mediaImage = FirebaseVisionImage.fromMediaImage(image.image!!, when (rotationDegrees) {
            0 -> 0
            90 -> 1
            180 -> 2
            270 -> 3
            else -> 0
        })
        detector.detectInImage(mediaImage).also {
            it.addOnSuccessListener(this)
            it.addOnFailureListener(this)
        }

    }

    override fun onFailure(p0: Exception) {
        Log.d("MLQRcodeAnalyzer", "onFailure:$p0")
    }

    override fun onSuccess(result: List<FirebaseVisionBarcode>) {
        Log.d("MLQRcodeAnalyzer", "onSuccess:$result")

    }
}
