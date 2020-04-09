package github.hotstu.camerax.qrcodec

import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 6/10/19
 */
class QRcodeAnalyzer() : ImageAnalysis.Analyzer {
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
        //YUV_420 is normally the input type here, but other YUV types are also supported in theory
        if (ImageFormat.YUV_420_888 != image.format && ImageFormat.YUV_422_888 != image.format && ImageFormat.YUV_444_888 != image.format) {
            Log.e("BarcodeAnalyzer", "expect YUV, now = ${image.format}")
            image.close()
            return
        }
        Log.e("BarcodeAnalyzer", "rotation!!! = ${image.imageInfo.rotationDegrees}")

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val height = image.height
        val width = image.width
        //TODO 调整crop的矩形区域，目前是全屏（全屏有更好的识别体验，但是在部分手机上可能OOM）
        val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(bitmap)
            Log.e("BarcodeAnalyzer", "resolved!!! = $result")
        } catch (e: Exception) {
            Log.d("BarcodeAnalyzer", "Error decoding barcode")
        } finally {
            image.close()
        }
    }

    /**
     * Helper extension function used to extract a byte array from an image plane buffer
     */
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    }
}
