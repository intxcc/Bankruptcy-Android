package cc.intx.bankruptcy

import android.graphics.Canvas
import android.util.Log
import android.view.SurfaceHolder

/**
 * Created by xiix on 20.01.18.
 */
class StartScreenAnimationThread(private var surfaceHolder: SurfaceHolder, private var surfaceView: StartScreenSurfaceView) : Thread() {

    var FPS = 40
    private var isRunning = false

    private var lastFrame: Long = 0

    fun setRunning(running: Boolean) {
        this.isRunning = running
    }

    override fun run() {
        var canvas: Canvas? = null
        while (isRunning) {
            if (System.currentTimeMillis() - lastFrame > 1000 / FPS) {
                lastFrame = System.currentTimeMillis()

                try {
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        canvas = surfaceHolder.lockHardwareCanvas()
                    } else {
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
                    if (FPS < 10) {
                        sleep(20)
                    }
                } catch (e: InterruptedException) {
                    Log.d("THREADB", "Interrupted sleep exception")
                }
            }
        }
    }

    override fun interrupt() {
        super.interrupt()

        Log.d("STARTTHREAD", "Thread was interrupted.")
    }

    init {
        isRunning = false
    }
}