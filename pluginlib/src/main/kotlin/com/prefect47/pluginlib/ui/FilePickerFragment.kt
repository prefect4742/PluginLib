package com.prefect47.pluginlib.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.prefect47.pluginlib.ui.preference.PluginListPreferenceFragment
import java.io.Serializable

typealias callbackFunc = (Uri) -> Unit

class FilePickerFragment : Fragment() {
    private val READ_REQUEST_CODE: Int = 42

    var mimeType: String? = null
    var layoutResId: Int = 0
    var callback: callbackFunc? = null

    companion object {
        private val KEY_MIMETYPE = "mime"
        private val KEY_LAYOUT = "layout"
        private val KEY_CALLBACK = "layout"

        fun create(mimeType: String, layoutResId: Int, callback: callbackFunc): FilePickerFragment =
            FilePickerFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_MIMETYPE, mimeType)
                    putInt(KEY_LAYOUT, layoutResId)
                    putSerializable(KEY_CALLBACK, callback as Serializable)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mimeType = arguments!!.getString(KEY_MIMETYPE)
        layoutResId = arguments!!.getInt(KEY_LAYOUT)
        callback = arguments!!.getSerializable(KEY_CALLBACK) as? callbackFunc
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, container)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            callback?.let { resultData?.data?.also { uri -> it(uri) } }
        }
    }

    fun pickFile(mimeType: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
        }

        startActivityForResult(intent, READ_REQUEST_CODE)
    }
}
