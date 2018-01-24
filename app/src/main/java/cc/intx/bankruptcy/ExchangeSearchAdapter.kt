package cc.intx.bankruptcy

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView



/**
 * Created by xiix on 23.01.18.
 */
class ExchangeSearchAdapter : RecyclerView.Adapter<ExchangeSearchAdapter.ViewHolder> {
    private var mDataset: Array<String>? = null

    class ViewHolder(// each data item is just a string in this case
            var mTextView: TextView) : RecyclerView.ViewHolder(mTextView)

    constructor(myDataset: Array<String>) {
        mDataset = myDataset
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ExchangeSearchAdapter.ViewHolder {
        var v: TextView = LayoutInflater.from(parent.context).inflate(R.layout.exchange_search_card, parent, false) as TextView
        var vh = ViewHolder(v)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        if (mDataset?.get(position) != null) {
            val s = mDataset?.get(position)
            holder?.mTextView?.setText(s)
        }
    }

    override fun getItemCount(): Int {
        return mDataset?.size ?: 0
    }

}