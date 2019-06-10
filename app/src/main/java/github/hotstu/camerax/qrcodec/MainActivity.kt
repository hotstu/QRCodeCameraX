package github.hotstu.camerax.qrcodec

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Rational
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 640))
        }.build()

        val analysisConfig = ImageAnalysisConfig.Builder().apply {
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            val analyzerThread = HandlerThread("BarcodeAnalyzer").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
        }.build()

        val rxPermissions = RxPermissions(this)
        val preview = Preview(previewConfig)
        val analysis = ImageAnalysis(analysisConfig)

        preview.setOnPreviewOutputUpdateListener {
            textureView.surfaceTexture = it.surfaceTexture
        }

        analysis.analyzer = QRcodeAnalyzer()
        rxPermissions.request(android.Manifest.permission.CAMERA)
                .subscribe {
                    CameraX.bindToLifecycle(this@MainActivity,
                            preview,
                            analysis)
                }
    }
}
