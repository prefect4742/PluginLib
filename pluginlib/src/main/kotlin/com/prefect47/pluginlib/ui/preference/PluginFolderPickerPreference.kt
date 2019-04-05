package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.preference.PreferenceViewHolder
import com.prefect47.pluginlib.R
import androidx.documentfile.provider.DocumentFile


/**
 * Preference that lets user pick a file from the storage framework.
 */
class PluginFolderPickerPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.pluginPreferenceStyle,
    defStyleRes: Int = R.style.PluginPreference
): PluginFilePickerPreferenceBase(context, attrs, defStyleAttr, defStyleRes) {
    init {
        pickerIntent.action = Intent.ACTION_OPEN_DOCUMENT_TREE
        widgetLayoutResource = R.layout.plugin_setting
    }

    private var summary: TextView? = null

    private fun updateSummary() {
        summary?.text = getUri()?.let { DocumentFile.fromTreeUri(context, it)?.name } ?: ""
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        summary = holder.itemView.findViewById<TextView>(android.R.id.summary)
            ?: throw IllegalStateException(
                "PluginFolderPickerPreference layout must contain an TextView with "
                        + "id @+id/android.R.id.summary"
            )
        updateSummary()
        holder.itemView.findViewById<AppCompatImageButton>(R.id.settings_button).apply {
            setOnClickListener { pick() }
            isEnabled = this@PluginFolderPickerPreference.isEnabled
        } ?: throw IllegalStateException(
            "PluginFolderPickerPreference layout must contain an ImageButton with "
                    + "id @+id/settings_button"
        )
    }

    override fun pick() {
        startActivityForResult(pickerIntent, pickerRequestCode)
    }

    override fun setUri(uri: Uri?) {
        super.setUri(uri)
        updateSummary()
    }
}
