package cc.intx.bankruptcy

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*

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

    private var newDataPoints = Array<PointF>(0, { PointF() })
    private var currentDataPoints = Array<PointF>(0, { PointF() })

    private var animationStepList = Array<PointF>(0, { PointF() })
    private var animationStepsLeft = 0
    private val animationSteps = 20

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

    // Canvas dimensions
    private var canvasWidth = 0f
    private var canvasHeight = 0f

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

        /*val n = 11
        val newDataPoints = Array<PointF>(n, { PointF() })
        for (i in 0 until n) {

        }


        for (i in 0 until n) {
            val newPoint = PointF()
            newPoint.set(i.toFloat() / 2, rnd.nextFloat() * 100)
            newDataPoints[i] = newPoint
        }

        setDataPoints(newDataPoints)*/
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
        SharedState.graphAnimationThread = animationThread

        animationThread.isRunning = true
        animationThread.start()
    }

    fun stopThread() {
        Log.d("GRAPH", "Stopping animation thread.")

        animationThread.isRunning = false
        animationThread.interrupt()

        SharedState.graphAnimationThread = null
    }

    //TESTFUN TODO DEBUG
    fun doDraw() {
        val rnd = Random()

        val n = 11

        val newDataPoints = Array<PointF>(n, { PointF() })
        for (i in 0 until n) {
            val newPoint = PointF()
            newPoint.set(i.toFloat() / 2, rnd.nextFloat() * 100)
            newDataPoints[i] = newPoint
        }

        setDataPoints(newDataPoints)
    }

    fun updateUi(canvas: Canvas): Unit {
        needUiUpdate = false

        Log.d("UI", "Updated ui.")

        canvasHeight = canvas.height.toFloat()
        canvasWidth = canvas.width.toFloat()

        uiBitmap = Bitmap.createBitmap(canvasWidth.toInt(), canvasHeight.toInt(), Bitmap.Config.ARGB_8888)
        uiCanvas = Canvas(uiBitmap)

        paddingH = canvasHeight / 10 * 2
        paddingW = canvasWidth / 10


        ///////////////////////
        // Draw Graph Border //
        paint.strokeWidth = 2f

        uiCanvas.drawLine(paddingW, canvasHeight - paddingH, canvasWidth - paddingW, canvasHeight - paddingH, paint)
        uiCanvas.drawLine(paddingW, canvasHeight - paddingH, paddingW, paddingH, paint)


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
        hInterval = (canvasHeight - (paddingH * 2)) / hStepCount

        // Draw horizontal grid and captions to canvas
        for (i in 0..hStepCount.toInt()) {
            val lineY = canvasHeight - paddingH - hInterval * i + hInterval / hStep * hMargin
            uiCanvas.drawLine(paddingW, lineY, canvasWidth - paddingW, lineY, paint)
            uiCanvas.drawText((lowerLimit + hStep * i).toString(), paddingW - tSize / 2 - 10, lineY + tSize / 3, paint)
        }

        // Text align for vertical grid
        paint.textAlign = Paint.Align.CENTER

        // Number of vertical lines
        val vStepCount = (rightLimit - leftLimit) / vStep

        // Spacing between each vertical line
        vInterval = (canvasWidth - (paddingW * 2)) / vStepCount

        // Draw vertical grid and captions to canvas
        for (i in 0..vStepCount.toInt()) {
            val lineX = paddingW + vInterval * i
            uiCanvas.drawLine(lineX, canvasHeight - paddingH, lineX, paddingH, paint)
            uiCanvas.drawText((leftLimit + vStep * i).toString(), lineX, canvasHeight - paddingH + tSize + 10, paint)
        }
    }

    fun setDataPoints(newDataPoints: Array<PointF>) {
        this.newDataPoints = newDataPoints

        graphNeedsUpdate = true
    }

    fun updateGraph(canvas: Canvas): Unit {
        graphNeedsUpdate = false

        Log.d("GRAPHANI", "Updating graph.")

        var startIndex = 0
        while (newDataPoints.size > startIndex && newDataPoints[startIndex].x < leftLimit) {
            startIndex++
        }

        var endIndex = startIndex
        while (newDataPoints.size > endIndex + 1 && newDataPoints[endIndex + 1].x <= rightLimit) {
            endIndex++
        }

        // Needed so the last point is rendering right (Last point is duplicated from createCatmullRomSpline)
        if (endIndex + 1 <= newDataPoints.size) {
            endIndex += 1
        }

        graphStartIndex = startIndex * graphSplineSteps
        graphEndIndex = endIndex * graphSplineSteps

        createGraphAnimation()
    }

    fun graphFromDataPoints(nDataPoints: Array<PointF>): Array<PointF> {
        val tempGraph = MiscFunctions.createCatmullRomSpline(nDataPoints, graphSplineSteps)
        val newGraph = Array<PointF>(tempGraph.size, { PointF() })

        for (i in graphStartIndex..graphEndIndex) {
            if (tempGraph.size > i) {
                val xVal = paddingW + (vInterval / vStep) * (tempGraph[i].x - leftLimit)
                var yVal: Float = if (tempGraph[i].y < 0) 0f else tempGraph[i].y
                yVal = canvasHeight - paddingH - (hInterval / hStep) * (yVal - lowerLimit)

                newGraph[i].set(xVal, yVal)
            }
        }

        return newGraph
    }

    fun createGraphAnimation() {
        if (currentDataPoints.isEmpty()) {
            currentDataPoints = newDataPoints
            animationStepsLeft = 0
            return
        }

        Log.d("GRAPHANI", "Creating Graph animation")

        animationStepList = Array<PointF>(currentDataPoints.size, { PointF() })
        for (i in 0 until animationStepList.size) {
            val xVal = (newDataPoints[i].x - currentDataPoints[i].x) / animationSteps
            val yVal = (newDataPoints[i].y - currentDataPoints[i].y) / animationSteps

            val p = PointF()
            p.set(xVal, yVal)
            animationStepList[i] = p
        }

        animationStepsLeft = animationSteps

        animationThread.setFramerate(100, 100000.toLong())
    }

    fun animateGraph() {
        for (i in 0 until currentDataPoints.size) {
            currentDataPoints[i].x += animationStepList[i].x
            currentDataPoints[i].y += animationStepList[i].y
        }

        animationStepsLeft--

        if (animationStepsLeft == 0) {
            animationThread.resetFramerate()
        }

        Log.d("GRAPHANI", "Animation steps left: " + animationStepsLeft)
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

            if (animationStepsLeft > 0) {
                animateGraph()
            }

            //////////////////
            // Render Graph //

            if (currentDataPoints.isNotEmpty()) {
                graph = graphFromDataPoints(currentDataPoints)

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