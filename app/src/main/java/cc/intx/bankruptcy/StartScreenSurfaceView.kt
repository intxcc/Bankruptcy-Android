package cc.intx.bankruptcy

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEventListener
import java.util.*
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.util.Log
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_start_screen.view.*


/**
 * Created by xiix on 20.01.18.
 */
class StartScreenSurfaceView : SurfaceView, SurfaceHolder.Callback, SensorEventListener {
    private lateinit var animationThread : StartScreenAnimationThread

    // Debug Stuff
    private val calculateFPS: Boolean = true

    private var paint = Paint()

    private var iconSize = 250

    private var xVelo = 0f
    private var yVelo = 0f

    private var xDelta = 0
    private var yDelta = 0

    private var bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    private var bCanvas= Canvas(bitmap)
    private var bgColor = Color.WHITE
    private var canvasInitialized = true

    private var lastDrawCall: Long = 0
    private var lastFpsCalc: Long = 0
    private var drawC = 0

    private lateinit var mSensorManager: SensorManager

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        startThread()
        canvasInitialized = false

        startScreenSurfaceView.setOnClickListener {
            canvasInitialized = false
        }
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        //canvasInitialized = false
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        stopThread()
    }

    fun initialize() {
        holder.addCallback(this)
        paint.isAntiAlias = true

        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event?.action == MotionEvent.ACTION_MOVE) {
            if (event.historySize > 1) {
                val xDelta = event.x - event.getHistoricalX(0)
                val yDelta = event.y - event.getHistoricalY(0)

                this.xDelta += xDelta.toInt()
                this.yDelta += yDelta.toInt()
            }
        }

        return true
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return
        }

        val mRotationMatrix = FloatArray(9)
        val orientationVals = FloatArray(3)

        SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, mRotationMatrix);
        SensorManager.getOrientation(mRotationMatrix, orientationVals);

        orientationVals[1] = Math.toDegrees(orientationVals[1].toDouble()).toFloat()
        orientationVals[2] = Math.toDegrees(orientationVals[2].toDouble()).toFloat()

        xVelo = orientationVals[2]
        yVelo = -orientationVals[1]
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // TODO Auto-generated method stub
    }

    fun initializeCanvas() {
        canvasInitialized = true

        var res: CryptoCurrencyInfo? = null
        while (res?.icon == null) {
            val randInt = Random().nextInt(SharedState.getCryptocurrencyList().size)
            res = SharedState.getCryptocurrencyList()[randInt]
        }

        (context as Activity).window.navigationBarColor = res.color

        val n = (bCanvas.height * (bCanvas.width + iconSize)) / (iconSize * iconSize)
        for (i in 0 until n) {
            val rIcon: Int? = res.icon

            var newDrawable: Drawable? = null
            if (rIcon != null) {
                newDrawable = resources.getDrawable(rIcon, null)
            }

            val row = i / (bCanvas.width / iconSize + 1)
            val cell = i % (bCanvas.width / iconSize + 1)

            val x = cell * iconSize + ((row % 2) * (iconSize / 2)) - (iconSize / 2)
            val y = row * iconSize

            newDrawable?.setBounds(x, y, x + (iconSize * 0.9).toInt(), y + (iconSize * 0.9).toInt())
            newDrawable?.draw(bCanvas)
        }

        Log.d("STARTCANVAS", "Canvas initialized.")
    }

    fun startThread() {
        Log.d("STARTTHREAD", "Starting thread.")

        animationThread = StartScreenAnimationThread(holder, this)
        animationThread.setRunning(true)
        animationThread.start()
    }

    fun stopThread() {
        Log.d("STARTTHREAD", "Stopping thread.")

        animationThread.setRunning(false)
        animationThread.interrupt()
    }

    fun updateCanvas(canvas: Canvas?): Unit {

        ////////////////////////////////
        // Calculate time coefficient //
        //   for timeaware movement   //
        drawC++
        var timeCoefficient: Double = 0.01
        if (lastDrawCall != 0.toLong()) {
            val passedTime = System.currentTimeMillis() - lastDrawCall
            timeCoefficient = passedTime / (1000.toDouble() / animationThread.FPS)

            if (drawC >= 80) {
                val FPS = Math.round((drawC.toDouble() / ((System.currentTimeMillis() - lastFpsCalc).toDouble() / 1000)) * 100).toFloat() / 100f
                Log.d("STARTANIM", "Current FPS is: " + FPS)

                lastFpsCalc = System.currentTimeMillis()
                drawC = 0
            }
        }
        lastDrawCall = System.currentTimeMillis()


        /////////////////
        // Draw canvas //
        canvas?.drawColor(bgColor)

        if (!canvasInitialized) {
            initializeCanvas()
        }

        if (canvas != null) {
            val bSize = bitmap.height
            val w = Math.ceil(canvas.width.toDouble() / bSize).toInt() + 2
            val h = Math.ceil(canvas.height.toDouble() / bSize).toInt() + 2

            for (i in 0 until w * h) {
                val x = i % w - 1
                val y = i / w - 1
                canvas.drawBitmap(bitmap,
                        (x * bSize).toFloat() + xDelta + (xVelo * 5).toInt(),
                        (y * bSize).toFloat() + yDelta + (yVelo * 5).toInt(), paint)
            }

            xDelta += 2
            yDelta += 2

            if (xDelta + (xVelo * 5) >= bSize) {
                xDelta -= bSize
            }

            if (xDelta + (xVelo * 5) <= -bSize) {
                xDelta += bSize
            }

            if (yDelta + (yVelo * 5) >= bSize) {
                yDelta -= bSize
            }

            if (yDelta + (yVelo * 5) <= -bSize) {
                yDelta += bSize
            }
        }
    }
}