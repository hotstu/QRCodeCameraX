package github.hotstu.camerax.qrcodec

import java.util.*

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @since 4/13/20
 * @desc
 */
class FpsDelegate {
    private val frameRateWindow = 8
    private val frameTimestamps = ArrayDeque<Long>(5)
    private var lastAnalyzedTimestamp = 0L
    var framesPerSecond: Double = -1.0

    fun tick() {
        // Keep track of frames analyzed
        val currentTime = System.currentTimeMillis()
        frameTimestamps.push(currentTime)

        // Compute the FPS using a moving average
        while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
        val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
        val timestampLast = frameTimestamps.peekLast() ?: currentTime
        framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

        // Analysis could take an arbitrarily long amount of time
        // Since we are running in a different thread, it won't stall other use cases

        lastAnalyzedTimestamp = frameTimestamps.first
    }
}