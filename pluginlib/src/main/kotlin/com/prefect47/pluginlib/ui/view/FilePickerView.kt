package com.prefect47.pluginlib.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.prefect47.pluginlib.R

/**
 * View that encapsulates a FilePickerFragment and forwards a layout to it
 */
class FilePickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = 0, defStyleRes: Int = 0) : View(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var mimeType: String
    private var layoutResId: Int = 0

    /*
    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.FilePicker,0, 0).apply {
                try {
                    mimeType = getString(R.styleable.FilePicker_android_mimeType)
                    layoutResId = getInt(R.styleable.FilePicker_android_layout, 0)
                } finally {
                    recycle()
                }
            }
        }
    }
    */

    // TODO: Create fragment and pass arguments to it
}
