package cc.intx.bankruptcy

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

/**
 * Created by xiix on 25.01.18.
 */
class MiscFunctions {
    companion object {
        fun convertDpToPx(dp: Int, context: Context): Int {
            return Math.round(dp * context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        private fun cubicBFun(i: Int, t: Float): Float {
            when (i) {
                -2 -> return (((-t + 3) * t - 3) * t + 1) / 6
                -1 -> return (((3 * t - 6) * t) * t + 4) / 6
                0 -> return (((-3 * t + 3) * t + 3) * t + 1) / 6
                1 -> return (t * t * t) / 6
            }
            return 0f //we only get here if an invalid i is specified
        }

        private fun catmullRomFun(i: Int, t: Float): Float {
            when (i) {
                -2 -> return ((-t + 2) * t - 1) * t / 2
                -1 -> return (((3 * t - 5) * t) * t + 2) / 2
                0 -> return ((-3 * t + 4) * t + 1) * t / 2
                1 -> return ((t - 1) * t * t) / 2
            }
            return 0f //we only get here if an invalid i is specified
        }

        private fun splinePoint(i: Int, t: Float, inputPoints: Array<PointF>): PointF {
            var xVal = 0f
            var yVal = 0f

            val startOffset = if (i < 2) -i else -2
            val endOffset = if (i + 1 >= inputPoints.size) 0 else 1
            for (j in startOffset..endOffset ) {
                xVal += catmullRomFun(j, t) * inputPoints[i + j].x
                yVal += catmullRomFun(j, t) * inputPoints[i + j].y
            }

            val p = PointF()
            p.set(xVal, yVal)
            return p
        }

        fun createCatmullRomSpline(iPoints: Array<PointF>, steps: Int): Array<PointF> {
            if (iPoints.size <= 2) {
                return Array<PointF>(0, {PointF()})
            }

            // So the ending is rendered correctly
            val inputPoints = iPoints.plusElement(iPoints.last())

            val outputPointCount = (inputPoints.size - 1) * steps + 1
            val outputPoints = Array<PointF>(outputPointCount, { PointF() })
            for (i in 0..steps) {
                outputPoints[i] = inputPoints[0]
            }

            for (i in 1 until (inputPoints.size - 1)) {
                for (j in 1..steps) {
                    outputPoints[i * steps + j] = splinePoint(i, j / steps.toFloat(), inputPoints)
                }
            }

            return outputPoints
        }

        fun hideSystemUi(window: Window) {
            window.addFlags(FLAG_HARDWARE_ACCELERATED)

            if (android.os.Build.VERSION.SDK_INT >= 26) {
                window.navigationBarColor = Color.WHITE
                window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            } else {
                window.decorView.systemUiVisibility =
                        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LOW_PROFILE)
            }
        }
    }
}