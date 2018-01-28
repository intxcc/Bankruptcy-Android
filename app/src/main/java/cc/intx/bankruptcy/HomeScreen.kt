package cc.intx.bankruptcy

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_home_screen.*


class HomeScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        exchangeButton.setOnClickListener {
            val intent = Intent(this, ExchangeHome::class.java)
            startActivity(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus:Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            MiscFunctions.adjustSystemUi(window)
        }
    }
}
