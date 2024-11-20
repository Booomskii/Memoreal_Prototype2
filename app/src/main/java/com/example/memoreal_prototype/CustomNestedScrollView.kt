package com.example.memoreal_prototype

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.NestedScrollView

class CustomNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        // Prevent the parent scroll view from scrolling when reaching the top or bottom of the child
        if (canScrollVertically(dy)) {
            super.onNestedPreScroll(target, dx, dy, consumed)
        }
    }
}
