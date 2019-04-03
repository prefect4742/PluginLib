package com.prefect47.pluginlib.impl

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.LayoutInflaterCompat
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.Nullable
import org.xmlpull.v1.XmlPullParser
import java.lang.reflect.Constructor

class DiscoverableLayoutInflater(
    original: LayoutInflater, private val appContext: Context, private val dContext: Context
): LayoutInflater(dContext), LayoutInflater.Factory2 {

    init {
        factory2 = this
    }

    override fun setFactory(factory: Factory?) {
        super.setFactory(factory)
    }

    override fun setFactory2(factory: Factory2?) {
        super.setFactory2(factory)
    }

    override fun cloneInContext(newContext: Context): LayoutInflater {
        return DiscoverableLayoutInflater(this, appContext, newContext)
    }

    override fun getContext(): Context {
        return dContext
    }

    override fun inflate(parser: XmlPullParser?, root: ViewGroup?): View {
        return super.inflate(parser, root)
    }

    override fun inflate(resource: Int, root: ViewGroup?, attachToRoot: Boolean): View {
        return super.inflate(resource, root, attachToRoot)
    }

    override fun inflate(@LayoutRes resource: Int, @Nullable root: ViewGroup?): View {
        return super.inflate(resource, root)
    }

    override fun inflate(parser: XmlPullParser?, root: ViewGroup?, attachToRoot: Boolean): View {
        return super.inflate(parser, root, attachToRoot)
    }

    /*
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)

        /*
        return if (name ==  "TextView") {
            AppCompatTextView(context, attrs)
        } else if (name == "ImageView") {
            AppCompatImageView(context, attrs)
        }
        else null
        /*
        var constructor: Constructor<out View>? = null
        var clazz: Class<out View>? = null
        try {
            clazz = contextWrapper.classLoader.loadClass(name).asSubclass<View>(View::class.java)
            constructor = clazz!!.getConstructor(*constructorSignature)
            constructor!!.setAccessible(true)
            return constructor.newInstance(contextWrapper, attrs)
        } catch (t: Throwable) {
            return null
        }
        */
        * */
    }
    */

    override fun onCreateView(name: String?, attrs: AttributeSet?): View? {
        return onCreateView(null, name, null, attrs)
    }

    override fun onCreateView(parent: View?, name: String?, attrs: AttributeSet?): View? {
        return onCreateView(parent, name, dContext, attrs)
    }

    override fun onCreateView(name: String?, context: Context?, attrs: AttributeSet?): View? {
        return onCreateView(null, name, context, attrs)
    }

    override fun onCreateView(parent: View?, name: String?, context: Context?, attrs: AttributeSet?): View? {
        var constructor: Constructor<out View>? = null
        var clazz: Class<out View>? = null

        if (clazz == null) {
            try {
                clazz = dContext.classLoader.loadClass(name).asSubclass<View>(View::class.java)
            } catch (e: ClassNotFoundException) {
            }
        }

        if (clazz == null) {
            try {
                clazz = dContext.classLoader.loadClass("android.widget.$name").asSubclass<View>(View::class.java)
            } catch (e: ClassNotFoundException) {
            }
        }

        if (clazz == null) {
            try {
                clazz = appContext.classLoader.loadClass(name).asSubclass<View>(View::class.java)
            } catch (e: ClassNotFoundException) {
            }
        }

        if (clazz == null) {
            try {
                clazz = appContext.classLoader.loadClass("android.widget.$name").asSubclass<View>(View::class.java)
            } catch (e: ClassNotFoundException) {
            }
        }

        return try {
            constructor = clazz!!.getConstructor(*constructorSignature)
            constructor!!.isAccessible = true
            constructor.newInstance(context, attrs)
        } catch (t: Throwable) {
            null
        }
    }

    companion object {

        private val constructorSignature = arrayOf<Class<*>>(Context::class.java, AttributeSet::class.java)
    }
}
