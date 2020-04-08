package github.hotstu.camerax.qrcodec

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

const val FLAGS_FULLSCREEN =
        View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

const val IMMERSIVE_FLAG_TIMEOUT = 500L

class MainActivity : AppCompatActivity() {

    var analyzerHandler: Handler? = null
    var analysis: ImageAnalysis? = null
    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        val rxPermissions = RxPermissions(this)
        rxPermissions.request(android.Manifest.permission.CAMERA)
                .subscribe {
                    textureView.post {
                        val cameraSelector = CameraSelector.Builder().requireLensFacing(
                            CameraSelector.LENS_FACING_BACK).build()
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
                        val metrics = DisplayMetrics().also { textureView.display.getRealMetrics(it) }
                        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
                        cameraProviderFuture.addListener(Runnable {
                            val preview = Preview.Builder().apply {
                                setTargetAspectRatio(screenAspectRatio)
                                setTargetRotation(textureView.display.rotation)
                            }.build()
                            // CameraProvider
                            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                            val analysis = ImageAnalysis.Builder().apply {
                                setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                                //val analyzerThread = HandlerThread("BarcodeAnalyzer").apply { start() }
                                //analyzerHandler = Handler(analyzerThread.looper)
                                //setCallbackHandler(analyzerHandler!!)
                                //setBackgroundExecutor {  }
                                setTargetAspectRatio(screenAspectRatio)
                                setTargetRotation(textureView.display.rotation)
                            }.build()


                            val googlePlayServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
                            if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
                                Log.d("MainActivity", "google play services avalable, using visionBarcodeDetector")
                                analysis.setAnalyzer(cameraExecutor, MLQRcodeAnalyzer())
                            } else {
                                Log.d("MainActivity", "google play services inavalable, fallback to zxing")
                                analysis.setAnalyzer(cameraExecutor, QRcodeAnalyzer())
                            }
                            // Must unbind the use-cases before rebinding them
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(this@MainActivity,
                                cameraSelector,
                                preview,
                                analysis)
                        }, ContextCompat.getMainExecutor(this))
                    }
                }
    }

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
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

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        textureView.postDelayed({
            textureView.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)
    }

    override fun onDestroy() {
        analyzerHandler?.removeCallbacksAndMessages(null)
        analyzerHandler?.looper?.quitSafely()
        cameraExecutor.shutdown()
        analysis?.clearAnalyzer()
        super.onDestroy()
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0


    }
}
