package cc.intx.bankruptcy

import android.graphics.Canvas
import android.provider.Settings
import android.util.Log
import android.view.SurfaceHolder

/**
 * Created by xiix on 20.01.18.
 */
class GraphAnimationThread : Thread {
    private var surfaceHolder : SurfaceHolder
    private var surfaceView : GraphSurfaceView

    private val defaultFPS = 4

    private var lastFrame: Long = 0
    private var resetFramerateTime: Long = 0
    var FPS = 4

    var isRunning = false

    constructor(surfaceHolder: SurfaceHolder, surfaceView: GraphSurfaceView) {
        this.surfaceHolder = surfaceHolder
        this.surfaceView = surfaceView

        isRunning = false
    }

    fun setFramerate(newFps: Int, milli: Long) {
        resetFramerateTime = System.currentTimeMillis() + milli
        lastFrame = 0
        FPS = newFps
    }

    fun resetFramerate() {
        resetFramerateTime = 0
        lastFrame = 0
        FPS = defaultFPS
    }

    override fun run() {
        var canvas: Canvas? = null

        lastFrame = System.currentTimeMillis()
        while (isRunning) {
            if (System.currentTimeMillis() > resetFramerateTime) {
                FPS = defaultFPS
            }

            if (System.currentTimeMillis() - lastFrame > 1000 / FPS) {
                lastFrame = System.currentTimeMillis()

                try {
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        // DEBUG Log.d("HARDWAREACC", "Using 'lockHardwareCanvas()'.")
                        canvas = surfaceHolder.lockHardwareCanvas()
                    } else {
                        // DEBUG Log.d("HARDWAREACC", "Not using 'lockHardwareCanvas()'. Using 'lockCanvas()' instead")
                        canvas = surfaceHolder.lockCanvas()
                    }
                    if (canvas != null) {
                        surfaceView.updateCanvas(canvas)
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
            } else {
                try {
                    if (FPS <= defaultFPS) {
                        sleep(20)
                    }
                } catch (e: InterruptedException) {
                    Log.d("THREADA", "Interrupted sleep exception")
                }
            }
        }
    }
}