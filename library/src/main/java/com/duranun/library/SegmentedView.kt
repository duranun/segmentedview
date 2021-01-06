package com.duranun.library

import android.animation.Animator
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.view.get


class SegmentedView : FrameLayout {
    private lateinit var selectionListener: (view: View, selectedIndex: Int) -> Unit
    private lateinit var selectionBar: View
    private lateinit var labelContainers: LinearLayout
    private var labelFontSize: Int = 14.toPx().toInt()
    private var labelTextColor: Int = Color.rgb(0, 0, 0)
    private var selectedTextColor: Int = Color.rgb(0, 0, 0)
    private var margins: Int = 0
    private var selectedIndex: Int = 0
    private var currentSelection: Int = selectedIndex
    private var segmentItems: Array<String> = arrayOf()
    private var segmentItemBackground: Drawable? = ColorDrawable(Color.rgb(0, 0, 0))


    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SegmentedView)
        try {
            segmentItemBackground = a.getDrawable(R.styleable.SegmentedView_segmentItemBackground)
            selectedIndex =
                a.getInteger(R.styleable.SegmentedView_defaultSelectedIndex, selectedIndex)
            labelFontSize = a.getDimensionPixelSize(
                R.styleable.SegmentedView_labelTextSize,
                labelFontSize
            )
            labelTextColor = a.getColor(R.styleable.SegmentedView_labelTextColor, labelTextColor)
            selectedTextColor =
                a.getColor(R.styleable.SegmentedView_selectedTextColor, selectedTextColor)
            margins = a.getDimensionPixelSize(R.styleable.SegmentedView_itemMargins, margins)
        } finally {
            a.recycle()
            addLabels()
        }
    }

    private fun addLabels() {
        labelContainers = LinearLayout(context)
        labelContainers.orientation = LinearLayout.HORIZONTAL
        val params = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        labelContainers.layoutParams = params
        labelContainers.weightSum = segmentItems.size.toFloat()
        segmentItems.forEachIndexed { index, text ->
            val textView = TextView(context)
            val textParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
            textView.gravity = Gravity.CENTER
            textView.layoutParams = textParams
            textView.text = text
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelFontSize.toFloat())
            textView.setTextColor(labelTextColor)
            textView.setOnClickListener {
                it.tag = index
                animateAndSetCurrent(index)
            }
            labelContainers.addView(textView)
        }
        addView(labelContainers)
        addItemSelector()
    }

    private fun animateAndSetCurrent(index: Int) {
        if (currentSelection == index) return
        val item = labelContainers[index]
        tag = index
        currentSelection = index
        selectionBar.animate().translationX(item.x)
            .setListener(animationListener)
    }

    private fun deselectAllItems() {
        labelContainers.children.forEach {
            val tv = it as TextView
            tv.setTextColor(labelTextColor)
        }
    }

    private fun setSelectedLayoutPos() {
        if (labelContainers.childCount == 0) return
        val selectedItem = labelContainers[selectedIndex]
        selectedItem.post {
            labelContainers[selectedIndex].callOnClick()
        }
        labelContainers.bringToFront()
    }

    private fun addItemSelector() {
        selectionBar = View(context)
        val itemParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        itemParams.setMargins(margins, margins, margins, margins)
        if (labelContainers.childCount == 0) return
        labelContainers[currentSelection].post {
            val selectorWidth = measuredWidth / segmentItems.size
            itemParams.width = selectorWidth - (margins * 2)
            selectionBar.layoutParams = itemParams
        }
        selectionBar.background = segmentItemBackground
        addView(selectionBar)

        setSelectedLayoutPos()
    }

    fun setOnSelectionListener(function: (view: View, selectedIndex: Int) -> Unit) {
        this.selectionListener = function
    }

    private val animationListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
            deselectAllItems()
        }

        override fun onAnimationEnd(animation: Animator?) {
            val selectedItem: TextView = labelContainers[currentSelection] as TextView
            selectedItem.setTextColor(selectedTextColor)
            selectionListener.invoke(selectedItem, currentSelection)

        }

        override fun onAnimationCancel(animation: Animator?) {
            // do nothing
        }

        override fun onAnimationRepeat(animation: Animator?) {
            // do nothing
        }
    }

    fun setItemsList(itemArray: Array<String>) {
        this.segmentItems = itemArray
        addLabels()
    }

    private var target: Float = 0f
    private var dX: Float = 0f
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = selectionBar.x - it.rawX
                }

                MotionEvent.ACTION_MOVE -> {
                    if (ev.eventTime - ev.downTime > CLICK_DURATION) {
                        val point = ev.rawX + dX
                        if (point > margins && point + selectionBar.measuredWidth < measuredWidth - margins) {
                            selectionBar.x = point
                            target = point
                        }

                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (labelContainers.childCount > 0 && ev.eventTime - ev.downTime > CLICK_DURATION) {
                        labelContainers.forEachIndexed { index, view ->
                             if(target >(view.left/2) && target<view.right){
                                animateAndSetCurrent(index)
                                return@forEachIndexed
                            }
                        }
                        selectionBar.animate()
                            .translationX(labelContainers[currentSelection].x)
                    }

                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    companion object {
        private const val CLICK_DURATION = 200
    }
}

private fun Int.toPx(): Float {
    val density = Resources.getSystem().displayMetrics.density
    return this * density
}
