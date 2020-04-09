package github.hotstu.camerax.qrcodec

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import java.io.ByteArrayOutputStream

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.flush()
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}




//TODO 解码有问题， 先去掉uv plane
fun decodeYUV420(rgba: IntArray, yuv420: ByteArray, width: Int, height: Int) {
    val frameSize = width * height
    var r: Int
    var g: Int
    var b: Int
    var y: Int
    var uvp: Int
    var u: Int
    var v: Int
    var j = 0
    var yp = 0
    while (j < height) {
        uvp = frameSize + (j shr 1) * width
        u = 0
        v = 0
        var i = 0
        while (i < width) {
            y = 0xff and yuv420[yp].toInt()
//                if (i and 1 == 0) {
//                    v = (0xff and yuv420[uvp++].toInt()) - 128
//                    u = (0xff and yuv420[uvp++].toInt()) - 128
//                }
            r = y + (1.370705f * v).toInt()
            g = y - (0.698001f * v - 0.337633f * u).toInt()
            b = y + (1.732446f * u).toInt()
            r = Math.max(0, Math.min(r, 255))
            g = Math.max(0, Math.min(g, 255))
            b = Math.max(0, Math.min(b, 255))
            rgba[yp] = Color.argb(255, r, g, b)
            i++
            yp++
        }
        j++
    }
}