package github.hotstu.camerax.qrcodec

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import io.github.hotstu.qrcodex.QRcodeAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private var analysis: ImageAnalysis? = null
    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    private val qrResult get() = findViewById<TextView>(R.id.qr_result)


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        val viewFinder = findViewById<PreviewView>(R.id.view_finder)
        val useMlKitDetector = intent.extras?.getBoolean("useMlKitDetector")?: false

        val rxPermissions = RxPermissions(this)
        rxPermissions.request(android.Manifest.permission.CAMERA)
                .subscribe {
                    viewFinder.post {
                        val cameraSelector = CameraSelector.Builder().requireLensFacing(
                            CameraSelector.LENS_FACING_BACK).build()
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
                        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
                        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
                        val rotation = viewFinder.display.rotation

                        cameraProviderFuture.addListener({
                            // CameraProvider
                            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().apply {
                                setTargetAspectRatio(screenAspectRatio)
                                setTargetRotation(rotation)
                            }.build()

                            // Attach the viewfinder's surface provider to preview use case
                            preview.setSurfaceProvider(viewFinder.surfaceProvider)

                            val analysis = ImageAnalysis.Builder().apply {
                                setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                                setTargetAspectRatio(screenAspectRatio)
                                setTargetRotation(rotation)
                            }.build()

                            if(useMlKitDetector){
                                Log.e("MainActivity", "MLQRcodeAnalyzer")
                                analysis.setAnalyzer(cameraExecutor,
                                    io.github.hotstu.qrcodex.ml.MLQRcodeAnalyzer(this::onResult)
                                )
                            } else{
                                Log.e("MainActivity", "QRcodeAnalyzer")
                                analysis.setAnalyzer(cameraExecutor, QRcodeAnalyzer(this::onResult))
                            }
                            // Must unbind the use-cases before rebinding them
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(this,
                                cameraSelector,
                                preview,
                                analysis)
                        }, ContextCompat.getMainExecutor(this))
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        window.decorView.postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = FLAGS_FULLSCREEN
            }
        }, IMMERSIVE_FLAG_TIMEOUT)
    }


    private fun onResult(value: String?) {
        qrResult.post {
            if(qrResult.text != value){
                qrResult.text = value
            }
        }
    }

    /**
     *  [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
     *  [androidx.camera.core.AspectRatio.Ratio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    override fun onDestroy() {
        analysis?.clearAnalyzer()
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private const val IMMERSIVE_FLAG_TIMEOUT = 500L

        @Suppress("DEPRECATION")
        private const val FLAGS_FULLSCREEN =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
}
