package cc.intx.bankruptcy

import android.graphics.Canvas
import android.view.SurfaceHolder

/**
 * Created by xiix on 20.01.18.
 */
class StartScreenAnimationThread : Thread {
    private var surfaceHolder : SurfaceHolder
    private var surfaceView : StartScreenSurfaceView

    private var isRunning = false

    constructor(surfaceHolder: SurfaceHolder, surfaceView: StartScreenSurfaceView) {
        this.surfaceHolder = surfaceHolder
        this.surfaceView = surfaceView

        isRunning = false
    }

    fun setRunning(running: Boolean) {
        this.isRunning = running
    }

    override fun run() {
        var canvas: Canvas? = null
        while (isRunning) {
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
        }
    }
}