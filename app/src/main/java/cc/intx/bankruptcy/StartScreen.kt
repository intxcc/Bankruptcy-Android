package cc.intx.bankruptcy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import kotlinx.android.synthetic.main.activity_start_screen.*

class StartScreen : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        testButton.setOnClickListener {
            //startScreenSurfaceView.stopThread()
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus:Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            MiscFunctions.adjustSystemUi(window, true)
        }
    }
}
