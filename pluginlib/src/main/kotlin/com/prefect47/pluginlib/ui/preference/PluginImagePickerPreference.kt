package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.preference.PreferenceViewHolder
import com.prefect47.pluginlib.R

/**
 * Preference with ImageButton that lets user pick an image.
 */
class PluginImagePickerPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.preferenceStyle, defStyleRes: Int = android.R.attr.preferenceStyle)
            : PluginPreference(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var mimeType: String // TODO: This might as well be hard-coded

    init {
        mimeType = "image/*"
        widgetLayoutResource = R.layout.plugin_imagepicker_widget
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        // TODO: Now we can call startActivityForResult()
        // TODO: We just need to inflate the inner view (ImageButton) and set a listener
        // TODO: The preference already has the right PluginWrapperContext :)
        // TODO: When result returns, save the URI to the pref and then load it into the ImageButton.

        val widgetFrame = holder.findViewById(R.id.button) as? ImageButton
        if (widgetFrame != null) {
            //with (fragment as PluginPreferencesFragment) {
            //    hello()
            //}
            //widgetFrame.addView(FilePickerFragment.create(mimeType, innerWidgetLayout))
            //val inflater = LayoutInflater.from(pluginContext)
            //inflater.inflate(innerWidgetLayout, widgetFrame)
        }
    }
}
