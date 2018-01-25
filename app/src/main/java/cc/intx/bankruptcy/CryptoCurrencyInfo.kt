package cc.intx.bankruptcy

/**
 * Created by xiix on 25.01.18.
 */
class CryptoCurrencyInfo {
    val long_name: String
    val short_name: String
    var icon: Int? = null

    constructor(long_name: String, short_name: String, icon: Int? = null) {
        this.long_name = long_name
        this.short_name = short_name
        this.icon = icon
    }
}