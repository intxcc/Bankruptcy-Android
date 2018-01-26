package cc.intx.bankruptcy

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.SearchView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_exchange_home.*
import android.R.attr.visibility
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import java.util.*


class ExchangeHome : AppCompatActivity() {

    lateinit var mAdapter: ExchangeSearchAdapter
    lateinit var mLayoutManager: RecyclerView.LayoutManager

    private var cryptoCurrencyList = ArrayList<CryptoCurrencyInfo>()
    private var selectedCryptoCurrency: CryptoCurrencyInfo? = null

    private lateinit var selectedLayoutHeightAnimator: ValueAnimator
    private lateinit var selectedLayoutColorAnimator: ValueAnimator
    private lateinit var titleTextViewColorAnimator: ValueAnimator

    private var isSearchFilterWaiting = false
    private var oldQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.setOnSystemUiVisibilityChangeListener{ visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_home)

        exchangeHomeWrapper.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(exchangeHomeWrapper.windowToken, 0)
        }

        cryptoCurrencyList.add(CryptoCurrencyInfo("Bitcoin", "BTC", Color.parseColor("#FF9900"), R.drawable.ic_bitcoin))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Litecoin", "LTC", Color.parseColor("#cbc6c6"), R.drawable.ic_litecoin))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Ethereum", "ETH", Color.parseColor("#627eea"), R.drawable.ic_ether))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Ethereum Classic", "ETC", Color.parseColor("#669073"), R.drawable.ic_eth_classic))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Monero", "XMR", Color.parseColor("#ff6600"), R.drawable.ic_monero))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Peercoin", "PPC", Color.parseColor("#3cb054"), R.drawable.ic_peercoin))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Ripple", "XRP", Color.parseColor("#00aae4"), R.drawable.ic_ripple))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Dash", "DASH", Color.parseColor("#1c75bc"), R.drawable.ic_dash))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Zcash", "ZEC", Color.parseColor("#ecb244"), R.drawable.ic_zcash))

        mLayoutManager = LinearLayoutManager(this);
        exSearchResultsRecyclerView.layoutManager = mLayoutManager

        mAdapter = ExchangeSearchAdapter(cryptoCurrencyList, ::selectCryptoCurrency, this)
        exSearchResultsRecyclerView.adapter = mAdapter

        initializeAnimators()
        exchangeSelectedCurrencySurfaceView.bgColor = Color.parseColor("#181818")

        btnTest.setOnClickListener {
            exchangeSelectedCurrencySurfaceView.doDraw()
        }

        btnTest2.setOnClickListener {
            // TEST TODO DELETE DEBUG
            selectCryptoCurrency(cryptoCurrencyList.get(Random().nextInt(5)))
        }

        btnTest3.setOnClickListener {
            // TEST TODO DELETE DEBUG
            exchangeSelectedCurrencySurfaceView.toggleThread()
        }

        exchangeSearchField.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!isSearchFilterWaiting) {
                    isSearchFilterWaiting = true

                    var waitTime: Long = 500
                    if (oldQuery == "") {
                        waitTime = 100
                    }

                    Handler().postDelayed({
                        if (newText != null) {
                            oldQuery = newText
                            mAdapter.filter(exchangeSearchField.query.toString())
                        }
                        isSearchFilterWaiting = false
                    }, waitTime)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    private fun initializeAnimators() {
        //////////////////////////////////
        // selectedLayoutHeightAnimator //
        selectedLayoutHeightAnimator = ValueAnimator.ofInt(MiscFunctions.convertDpToPx(80, this), MiscFunctions.convertDpToPx(300, this))

        selectedLayoutHeightAnimator.addUpdateListener { valueAnimator ->
            val layoutParams = exchangeSelectedCurrencyLayout.layoutParams
            layoutParams.height = valueAnimator.animatedValue as Int
            exchangeSelectedCurrencyLayout.layoutParams = layoutParams
        }

        selectedLayoutHeightAnimator.addListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator?) {
            }
            override fun onAnimationRepeat(animation: Animator?) {
            }
            override fun onAnimationEnd(animation: Animator?) {
            }
            override fun onAnimationCancel(animation: Animator?) {
            }
        })

        /*/////////////////////////////////
        // selectedLayoutColorAnimator //
        selectedLayoutColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.parseColor("#181818"), Color.parseColor("#dddddd"))

        selectedLayoutColorAnimator.addUpdateListener { valueAnimator ->
            exchangeSelectedCurrencyLayout.setBackgroundColor(valueAnimator.animatedValue as Int)
        }*/

        ////////////////////////////////
        // titleTextViewColorAnimator //
        titleTextViewColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.parseColor("#ffffffff"), Color.parseColor("#66ffffff"))

        titleTextViewColorAnimator.addUpdateListener { valueAnimator ->
            exchangeHomeTitleTextView.setTextColor(valueAnimator.animatedValue as Int)
        }
    }

    fun selectCryptoCurrency(selection: CryptoCurrencyInfo?) {
        if (selectedCryptoCurrency == null && selection != null) {
            selectedLayoutHeightAnimator.start()
            titleTextViewColorAnimator.start()


            selectedLayoutColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.parseColor("#181818"), selection.color)

            selectedLayoutColorAnimator.addUpdateListener { valueAnimator ->
                exchangeSelectedCurrencyLayout.setBackgroundColor(valueAnimator.animatedValue as Int)
                exchangeSelectedCurrencySurfaceView.bgColor = valueAnimator.animatedValue as Int
            }

            selectedLayoutColorAnimator.start()
        } else if (selectedCryptoCurrency != null && selection == null) {
            selectedLayoutHeightAnimator.reverse()
            titleTextViewColorAnimator.reverse()


            selectedLayoutColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), selectedCryptoCurrency?.color, Color.parseColor("#181818"))

            selectedLayoutColorAnimator.addUpdateListener { valueAnimator ->
                exchangeSelectedCurrencyLayout.setBackgroundColor(valueAnimator.animatedValue as Int)
                exchangeSelectedCurrencySurfaceView.bgColor = valueAnimator.animatedValue as Int
            }

            selectedLayoutColorAnimator.start()
        } else if (selectedCryptoCurrency != null && selection != null) {
            selectedLayoutColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), selectedCryptoCurrency?.color, selection.color)

            selectedLayoutColorAnimator.addUpdateListener { valueAnimator ->
                exchangeSelectedCurrencyLayout.setBackgroundColor(valueAnimator.animatedValue as Int)
                exchangeSelectedCurrencySurfaceView.bgColor = valueAnimator.animatedValue as Int
            }

            selectedLayoutColorAnimator.start()

        }

        selectedCryptoCurrency = selection
    }
}
