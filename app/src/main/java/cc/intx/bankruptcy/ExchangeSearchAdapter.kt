package cc.intx.bankruptcy

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.animation.ObjectAnimator
import android.graphics.Color
import android.util.DisplayMetrics


/**
 * Created by xiix on 23.01.18.
 */
class ExchangeSearchAdapter : RecyclerView.Adapter<ExchangeSearchAdapter.ViewHolder> {
    private var mDataset = ArrayList<CryptoCurrencyInfo>()
    private var mDatasetFiltered = ArrayList<CryptoCurrencyInfo>()

    private var markedForDeletion = ArrayList<CryptoCurrencyInfo>()
    private var markedForInsertion = ArrayList<CryptoCurrencyInfo>()
    private var markedForInsertionCount = 0

    private var context: Context
    private var selectCryptoCurrency: (c: CryptoCurrencyInfo?) -> Unit

    private var selection: CryptoCurrencyInfo? = null

    class ViewHolder(// each data item is just a string in this case
            var mFrameLayout: FrameLayout) : RecyclerView.ViewHolder(mFrameLayout)

    constructor(myDataset: ArrayList<CryptoCurrencyInfo>, selectCryptoCurrency: (c: CryptoCurrencyInfo?) -> Unit, context: Context) {
        mDataset = myDataset
        this.selectCryptoCurrency = selectCryptoCurrency
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ExchangeSearchAdapter.ViewHolder {
        val v: FrameLayout = LayoutInflater.from(parent.context).inflate(R.layout.exchange_search_card, parent, false) as FrameLayout
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val item = mDatasetFiltered.get(position)

        holder?.mFrameLayout?.findViewById<TextView>(R.id.info_text)?.text = item.long_name
        holder?.mFrameLayout?.findViewById<TextView>(R.id.short_text)?.text = item.short_name

        val icon = item.icon
        if (icon != null) holder?.mFrameLayout?.findViewById<ImageView>(R.id.bg_image)?.setBackgroundResource(icon)

        if (selection != null && holder?.itemView != null && item == selection) {
            holder.itemView.elevation = 0f
        } else if (holder?.itemView != null) {
            holder.itemView.elevation = 2f
        }

        if (holder?.itemView != null) {
            holder.itemView.setOnClickListener {
                SharedState.graphAnimationThread?.setFramerate(30, 500)

                if (selection == item) {
                    selectCryptoCurrency(null)
                    selection = null
                    notifyDataSetChanged()
                } else {
                    selectCryptoCurrency(item)
                    selection = item
                    notifyDataSetChanged()
                }
            }

            if (markedForInsertion.contains(item)) {
                markedForInsertion.remove(item)
                setInsertAnimation(holder.itemView)
            }

            if (markedForDeletion.contains(item)) {
                setDeleteAnimation(holder.itemView, item)
            }
        }
    }

    private fun setDeleteAnimation (viewToAnimate: View, item: CryptoCurrencyInfo) {
        val alphaAnimation = ObjectAnimator.ofFloat(viewToAnimate, "alpha", 0f)
        alphaAnimation.duration = 300
        alphaAnimation.start()

        val oldHeight = viewToAnimate.measuredHeight
        val heightAnimation = ValueAnimator.ofInt(viewToAnimate.measuredHeight, 0)

        heightAnimation.addUpdateListener { valueAnimator ->
            val layoutParams = viewToAnimate.layoutParams
            layoutParams.height = valueAnimator.animatedValue as Int
            viewToAnimate.layoutParams = layoutParams
        }

        heightAnimation.addListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator?) {
            }
            override fun onAnimationRepeat(animation: Animator?) {
            }
            override fun onAnimationEnd(animation: Animator?) {
                val layoutParams = viewToAnimate.layoutParams
                layoutParams.height = oldHeight
                viewToAnimate.layoutParams = layoutParams

                alphaAnimation.cancel()
                viewToAnimate.alpha = 1f

                if (markedForDeletion.contains(item)) {
                    markedForDeletion.remove(item)
                    mDatasetFiltered.remove(item)
                    notifyDataSetChanged()
                }
            }
            override fun onAnimationCancel(animation: Animator?) {
            }
        })

        heightAnimation.startDelay = alphaAnimation.duration
        heightAnimation.duration = 200
        heightAnimation.start()
    }

    private fun setInsertAnimation(viewToAnimate: View) {
        viewToAnimate.alpha = 0f
        val heightAnimation = ValueAnimator.ofInt(0, MiscFunctions.convertDpToPx(50, context))

        heightAnimation.addUpdateListener { valueAnimator ->
            val layoutParams = viewToAnimate.layoutParams
            layoutParams.height = valueAnimator.animatedValue as Int
            viewToAnimate.layoutParams = layoutParams
        }

        heightAnimation.addListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator?) {
            }
            override fun onAnimationRepeat(animation: Animator?) {
            }
            override fun onAnimationEnd(animation: Animator?) {
                viewToAnimate.alpha = 1f

                val insertAnimation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
                insertAnimation.duration = 300
                viewToAnimate.startAnimation(insertAnimation)
            }
            override fun onAnimationCancel(animation: Animator?) {
            }
        })

        heightAnimation.duration = 100 + (markedForInsertionCount - markedForInsertion.size) * 50.toLong()
        heightAnimation.start()
    }

    fun filter(s: String) {
        if (s.isNotEmpty()) {
            for (item in mDataset) {
                if (item.long_name.indexOf(s, 0, true) >= 0) {
                    if (!mDatasetFiltered.contains(item)) {
                        mDatasetFiltered.add(item)
                        markedForInsertion.add(item)
                    }

                    if (markedForDeletion.contains(item)) {
                        markedForDeletion.remove(item)
                        markedForInsertion.add(item)
                    }
                } else if (mDatasetFiltered.contains(item)) {
                    markedForDeletion.add(item)
                    markedForInsertion.remove(item)
                }
            }
        } else {
            //mDatasetFiltered.clear()
            for (item in mDatasetFiltered) {
                markedForDeletion.add(item)
                markedForInsertion.remove(item)
            }
        }

        val sortedList = mDatasetFiltered.sortedWith(compareBy({it.long_name}))
        mDatasetFiltered = ArrayList(sortedList)

        markedForInsertionCount = markedForInsertion.size

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mDatasetFiltered.size
    }

}