package cc.intx.bankruptcy

import android.graphics.Color

/**
 * Created by xiix on 26.01.18.
 */
class SharedState {
    companion object {
        private var cryptoCurrencyList = ArrayList<CryptoCurrencyInfo>()

        var graphAnimationThread: GraphAnimationThread? = null

        private fun createCryptoCurrencyList() {
            cryptoCurrencyList.add(CryptoCurrencyInfo("Bitcoin", "BTC", Color.parseColor("#FF9900"), R.drawable.ic_bitcoin))
            cryptoCurrencyList.add(CryptoCurrencyInfo("Litecoin", "LTC", Color.parseColor("#cbc6c6"), R.drawable.ic_litecoin))
            cryptoCurrencyList.add(CryptoCurrencyInfo("Ethereum", "ETH", Color.parseColor("#627eea"), R.drawable.ic_ether))
            cryptoCurrencyList.add(CryptoCurrencyInfo("Ethereum Classic", "ETC", Color.parseColor("#669073"), R.drawable.ic_eth_classic))
            cryptoCurrencyList.add(CryptoCurrencyInfo("Monero", "XMR", Color.parseColor("#ff6600"), R.drawable.ic_monero))
            cryptoCurrencyList.add(CryptoCurrencyInfo("Peercoin", "PPC", Color.parseColor("#3cb054"), R.drawable.ic_peercoin))
            cryptoCurrencyList.add(CryptoCurrencyInfo("Ripple", "XRP", Color.parseColor("#00aae4"), R.drawable.ic_ripple))
            cryptoCurrencyList.add(CryptoCurrencyInfo("Dash", "DASH", Color.parseColor("#1c75bc"), R.drawable.ic_dash))
            cryptoCurrencyList.add(CryptoCurrencyInfo("Zcash", "ZEC", Color.parseColor("#ecb244"), R.drawable.ic_zcash))
        }

        fun getCryptocurrencyList(): ArrayList<CryptoCurrencyInfo> {
            if (cryptoCurrencyList.isEmpty()) {
                createCryptoCurrencyList()
            }

            return cryptoCurrencyList
        }
    }
}