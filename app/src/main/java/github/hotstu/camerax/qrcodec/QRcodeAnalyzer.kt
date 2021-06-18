package github.hotstu.camerax.qrcodec

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import kotlin.math.min


/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 6/10/19
 */
class QRcodeAnalyzer(private val resultHandler: (String?) -> Unit) : ImageAnalysis.Analyzer {
    private var mYBuffer = ByteArray(0)
    private val fpsDelegate = FpsDelegate()

    private val reader: MultiFormatReader = MultiFormatReader().apply {
        val map = mapOf<DecodeHintType, Collection<BarcodeFormat>>(
            Pair(DecodeHintType.POSSIBLE_FORMATS, arrayListOf(BarcodeFormat.QR_CODE))
        )
        setHints(map)
    }

    /**
     * Analyzes an image to produce a result.
     *
     * <p>The caller is responsible for ensuring this analysis method can be executed quickly
     * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
     * images will not be acquired and analyzed.
     *
     * <p>The image passed to this method becomes invalid after this method returns. The caller
     * should not store external references to this image, as these references will become
     * invalid.
     *
     * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
     * call image.close() on received images when finished using them. Otherwise, new images
     * may not be received or the camera may stall, depending on back pressure setting.
     *
     */
    override fun analyze(image: ImageProxy) {
        if (ImageFormat.YUV_420_888 != image.format) {
            Log.e("BarcodeAnalyzer", "expect YUV_420_888, now = ${image.format}")
            image.close()
            return
        }
        fpsDelegate.tick()
        val height = image.height
        val width = image.width
        //TODO 调整crop的矩形区域，目前是全屏（全屏有更好的识别体验，但是在部分手机上可能OOM）
        ///PlanarYUVLuminaceSource only care the Y plane
        val source = PlanarYUVLuminanceSource(image.toYBuffer(), width, height, 0, 0, width, height, false)
        image.close()
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(bitmap)
            resultHandler.invoke(result.text)
        } catch (e: Exception) {
            resultHandler.invoke(e.message)
        }
        Log.d("ZxingQRcodeAnalyzer", "frames: ${fpsDelegate.framesPerSecond}")
    }

    private fun ImageProxy.toYBuffer(): ByteArray {
        val yPlane = planes[0]
        val yBuffer = yPlane.buffer
        yBuffer.rewind()
        val ySize = yBuffer.remaining()
        var position = 0
        if (mYBuffer.size != ySize) {
            Log.w("BarcodeAnalyzer", "swap buffer since size ${mYBuffer.size} != $ySize")
            mYBuffer = ByteArray(ySize)
        }
        // Add the full y buffer to the array. If rowStride > 1, some padding may be skipped.
        for (row in 0 until height) {
            yBuffer.get(mYBuffer, position, width)
            position += width
            yBuffer.position(min(ySize, yBuffer.position() - width + yPlane.rowStride))
        }
        return mYBuffer
    }

}
