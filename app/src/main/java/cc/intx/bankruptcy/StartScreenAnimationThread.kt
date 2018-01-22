package cc.intx.bankruptcy

import android.graphics.Canvas
import android.view.SurfaceHolder

/**
 * Created by xiix on 20.01.18.
 */
class StartScreenAnimationThread : Thread {
    private var surfaceHolder : SurfaceHolder
    var surfaceView : StartScreenSurfaceView

    private var run = false

    constructor(surfaceHolder: SurfaceHolder, surfaceView: StartScreenSurfaceView) {
        this.surfaceHolder = surfaceHolder
        this.surfaceView = surfaceView

        run = false
    }

    fun setRunning(run: Boolean) {
        this.run = run
    }

    override fun run() {
        var canvas: Canvas? = null
        while (run) {
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