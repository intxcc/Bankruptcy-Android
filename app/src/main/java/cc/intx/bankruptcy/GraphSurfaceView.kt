package cc.intx.bankruptcy

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by xiix on 20.01.18.
 */
class GraphSurfaceView : SurfaceView, SurfaceHolder.Callback {
    private lateinit var animationThread : GraphAnimationThread
    private var paint = Paint()
    private var graphPaint = Paint()
    private var graphCirclePaint = Paint()

    private var uiBitmap = Bitmap.createBitmap(1500, 1500, Bitmap.Config.ARGB_8888)
    private var uiCanvas = Canvas(uiBitmap)

    private var dataPoints = ArrayList<PointF>()

    private var graph = Array<PointF>(0, { PointF() })
    private var graphStartIndex = -1
    private var graphEndIndex = -1

    // Smoothing of graph
    private var graphSplineSteps = 5

    // View limit
    private var lowerLimit = 0f
    private var upperLimit = 100f
    private var leftLimit = 0f
    private var rightLimit = 5f

    // Units between grid lines
    private var hStep = 10f
    private var vStep = 1f

    // Padding of Graph to outline
    private var paddingH = 0f
    private var paddingW = 0f

    // Distance between grid lines
    private var hInterval = 0f
    private var vInterval = 0f

    private var needUiUpdate = false
    private var graphNeedsUpdate = false

    var bgColor = Color.WHITE

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        startThread()
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        needUiUpdate = true
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        stopThread()
    }

    fun initialize() {
        holder.addCallback(this)
        paint.isAntiAlias = true

        paint.color = Color.WHITE

        graphPaint.strokeWidth = 2f
        graphPaint.style = Paint.Style.STROKE
        graphPaint.color = Color.WHITE

        graphCirclePaint.style = Paint.Style.FILL_AND_STROKE
        graphCirclePaint.color = Color.WHITE
        graphCirclePaint.textSize = 30f
        graphCirclePaint.textAlign = Paint.Align.CENTER
    }

    fun toggleThread() {
        if (animationThread.isRunning) {
            stopThread()
        } else {
            startThread()
        }
    }

    fun startThread() {
        Log.d("GRAPH", "Starting animation thread.")

        animationThread = GraphAnimationThread(holder, this)
        animationThread.isRunning = true
        animationThread.start()
    }

    fun stopThread() {
        Log.d("GRAPH", "Stopping animation thread.")

        animationThread.isRunning = false
        animationThread.interrupt()
    }

    //TESTFUN TODO DEBUG
    fun doDraw() {
        val rnd = Random()

        dataPoints = ArrayList<PointF>()
        for (i in 0..10) {
            val newPoint = PointF()
            newPoint.set(i.toFloat() / 2, rnd.nextFloat() * 100)
            dataPoints.add(newPoint)
        }

        graphNeedsUpdate = true
    }

    fun updateUi(canvas: Canvas): Unit {
        needUiUpdate = false

        Log.d("UI", "Updated ui.")

        val h = canvas.height.toFloat()
        val w = canvas.width.toFloat()

        uiBitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        uiCanvas = Canvas(uiBitmap)

        paddingH = h / 10 * 2
        paddingW = w / 10


        ///////////////////////
        // Draw Graph Border //
        paint.strokeWidth = 2f

        uiCanvas.drawLine(paddingW, h - paddingH, w - paddingW, h - paddingH, paint)
        uiCanvas.drawLine(paddingW, h - paddingH, paddingW, paddingH, paint)


        ///////////////
        // Draw Grid //
        paint.strokeWidth = 0.2f

        val tSize = 20f
        paint.textSize = tSize
        paint.textAlign = Paint.Align.RIGHT

        // Margin in case limits are not dividable by hStep
        val hMargin = lowerLimit % hStep
        lowerLimit -= hMargin

        // Number of horizontal lines
        val hStepCount = ((upperLimit - lowerLimit) / hStep)

        // Spacing between each horizontal line
        hInterval = (h - (paddingH * 2)) / hStepCount

        // Draw horizontal grid and captions to canvas
        for (i in 0..hStepCount.toInt()) {
            val lineY = h - paddingH - hInterval * i + hInterval / hStep * hMargin
            uiCanvas.drawLine(paddingW, lineY, w - paddingW, lineY, paint)
            uiCanvas.drawText((lowerLimit + hStep * i).toString(), paddingW - tSize / 2 - 10, lineY + tSize / 3, paint)
        }

        // Text align for vertical grid
        paint.textAlign = Paint.Align.CENTER

        // Number of vertical lines
        val vStepCount = (rightLimit - leftLimit) / vStep

        // Spacing between each vertical line
        vInterval = (w - (paddingW * 2)) / vStepCount

        // Draw vertical grid and captions to canvas
        for (i in 0..vStepCount.toInt()) {
            val lineX = paddingW + vInterval * i
            uiCanvas.drawLine(lineX, h - paddingH, lineX, paddingH, paint)
            uiCanvas.drawText((leftLimit + vStep * i).toString(), lineX, h - paddingH + tSize + 10, paint)
        }
    }

    fun updateGraph(canvas: Canvas): Unit {
        graphNeedsUpdate = false

        var startIndex = 0
        while (dataPoints.size > startIndex && dataPoints[startIndex].x < leftLimit) {
            startIndex++
        }

        var endIndex = startIndex
        while (dataPoints.size > endIndex + 1 && dataPoints[endIndex + 1].x <= rightLimit) {
            endIndex++
        }

        // Needed so the last point is rendering right (Last point is duplicated from createCatmullRomSpline)
        if (endIndex + 1 <= dataPoints.size) {
            endIndex += 1
        }

        val tempGraph = MiscFunctions.createCatmullRomSpline(dataPoints, graphSplineSteps)
        graph = Array<PointF>(tempGraph.size, { PointF() })

        graphStartIndex = startIndex * graphSplineSteps
        graphEndIndex = endIndex * graphSplineSteps

        for (i in graphStartIndex..graphEndIndex) {
            if (tempGraph.size > i) {
                val xVal = paddingW + (vInterval / vStep) * (tempGraph[i].x - leftLimit)
                var yVal: Float = if (tempGraph[i].y < 0) 0f else tempGraph[i].y
                yVal = canvas.height - paddingH - (hInterval / hStep) * (yVal - lowerLimit)

                graph[i].set(xVal, yVal)
            }
        }
    }

    fun updateCanvas(canvas: Canvas?): Unit {
        canvas?.drawColor(bgColor)
        canvas?.drawBitmap(uiBitmap, 0f, 0f, paint)

        if (canvas != null) {
            if (needUiUpdate) {
                updateUi(canvas)
            }

            if (graphNeedsUpdate) {
                updateGraph(canvas)
            }

            //////////////////
            // Render Graph //

            if (graph.isNotEmpty()) {
                val path = Path()
                for (i in graphStartIndex..graphEndIndex) {
                    val xVal = graph[i].x
                    val yVal = graph[i].y

                    if (i % graphSplineSteps == 0) {
                        canvas.drawText("●", xVal, yVal + 10, graphCirclePaint)
                    }

                    if (path.isEmpty) {
                        path.moveTo(xVal, yVal)
                    } else {
                        path.lineTo(xVal, yVal)
                    }
                }

                canvas.drawPath(path, graphPaint)
            }
        }
    }
}