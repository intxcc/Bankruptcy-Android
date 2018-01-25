package cc.intx.bankruptcy

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.SearchView
import kotlinx.android.synthetic.main.activity_exchange_home.*


class ExchangeHome : AppCompatActivity() {

    lateinit var mAdapter: ExchangeSearchAdapter
    lateinit var mLayoutManager: RecyclerView.LayoutManager

    private var cryptoCurrencyList = ArrayList<CryptoCurrencyInfo>()

    private var isSearchFilterWaiting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_home)

        cryptoCurrencyList.add(CryptoCurrencyInfo("Bitcoin", "BTC", R.drawable.ic_bitcoin))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Litecoin", "LTC", R.drawable.ic_litecoin))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Ethereum", "ETH", R.drawable.ic_ether))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Ethereum Classic", "ETC", R.drawable.ic_eth_classic))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Monero", "XMR", R.drawable.ic_monero))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Peercoin", "PPC", R.drawable.ic_peercoin))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Ripple", "XRP", R.drawable.ic_ripple))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Dash", "DASH", R.drawable.ic_dash))
        cryptoCurrencyList.add(CryptoCurrencyInfo("Zcash", "ZEC", R.drawable.ic_zcash))

        mLayoutManager = LinearLayoutManager(this);
        exSearchResultsRecyclerView.layoutManager = mLayoutManager

        mAdapter = ExchangeSearchAdapter(cryptoCurrencyList, this)
        exSearchResultsRecyclerView.adapter = mAdapter

        btnTest.setOnClickListener {
            //dataset.add("sfuhiui")
            //mAdapter.notifyDataSetChanged()
        }

        exchangeSearchField.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!isSearchFilterWaiting) {
                    isSearchFilterWaiting = true

                    Handler().postDelayed({
                        if (newText != null) mAdapter.filter(exchangeSearchField.query.toString())
                        isSearchFilterWaiting = false
                    }, 500)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}
