package com.prefect47.pluginlib.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageButton
import kotlin.math.max

/**
 * Square ImageButton that will use the largest of its dimensions on both axis.
 */
class SquareImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.imageButtonStyle) : AppCompatImageButton(context, attrs, defStyleAttr) {
    init {
        viewTreeObserver.addOnPreDrawListener(ViewTreeObserver.OnPreDrawListener {
            if (width != height) {
                val maxSize = max(width, height)
                layoutParams.apply {
                    width = maxSize
                    height = maxSize
                }
                requestLayout()
                return@OnPreDrawListener false
            }
            true
        })
    }
}
