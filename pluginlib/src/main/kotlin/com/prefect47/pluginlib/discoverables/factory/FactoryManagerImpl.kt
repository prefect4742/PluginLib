package com.prefect47.pluginlib.discoverables.factory

import android.util.Log
import com.prefect47.pluginlib.Control
import com.prefect47.pluginlib.discoverables.factory.FactoryDiscoverable
import com.prefect47.pluginlib.DiscoverableManager
import com.prefect47.pluginlib.Manager
import com.prefect47.pluginlib.annotations.Requirements
import com.prefect47.pluginlib.annotations.Requires
import com.prefect47.pluginlib.discoverables.factory.FactoryDiscoverableInfo
import com.prefect47.pluginlib.discoverables.factory.FactoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class FactoryManagerImpl @Inject constructor(
    private val control: Control, private val manager: Manager,
    private val discoverableInfoFactory: FactoryDiscoverableInfo.Factory
): FactoryManager {
    companion object {
        const val TAG = "FactoryManager"
    }

    inner class FactoryAction(
        val action: String,
        var discoverableManager: DiscoverableManager<FactoryDiscoverable, FactoryDiscoverableInfo>?
    ): FactoryDiscoverableInfo.Listener {
        override fun onStartDiscovering() {
            control.debug("Starting tracking factories with $action")
        }

        override fun onDoneDiscovering() {
            control.debug("Started tracking factories with $action")
        }

        override fun onDiscovered(info: FactoryDiscoverableInfo) {
            if (control.debugEnabled) Log.d(TAG, "Found factory ${info.component.className}")
            factories.add(info.factory)
        }

        override fun onRemoved(info: FactoryDiscoverableInfo) {
            if (control.debugEnabled) Log.d(TAG, "Factory ${info.component.className} was removed")
            factories.remove(info.factory)
        }
    }

    private val factoryActions = ArrayList<FactoryAction>()
    private val addedClasses = HashMap<String, KClass<*>>()
    private val addedRequire = HashMap<KClass<*>, List<FactoryDiscoverable.Require>>()

    override val factories = java.util.ArrayList<FactoryDiscoverable>()

    override fun track(action: String) {
        factoryActions.add(FactoryAction(action, null))
    }

    override fun addClass(className: String, cls: KClass<*>) {
        addedClasses[className] = cls
    }

    override fun findClass(className: String): KClass<*>? {
        factories.forEach {  list ->
            list.classMap[className]?.let { return it }
        }
        control.staticProviders.forEach { list ->
            list.classMap[className]?.let { return it }
        }
        return addedClasses[className]
    }

    override fun findRequirements(cls: KClass<*>): List<FactoryDiscoverable.Require>? {
        factories.forEach { factory -> factory.requirements[cls]?.let { return it } }

        control.debug("findRequirements($cls reflection fallback")
        return addedRequire[cls]
            ?: cls.findAnnotation<Requires>()?.let { a -> listOf(FactoryDiscoverable.Require(a.target, a.version)) }
            ?: cls.findAnnotation<Requirements>()?.value?.map { a -> FactoryDiscoverable.Require(a.target, a.version) }
            ?.also { addedRequire[cls] = it }
    }

    override suspend fun start() {
        if (control.debugEnabled) Log.d(TAG, "Starting")

        withContext(Dispatchers.Default) {
            factoryActions.forEach {
                it.discoverableManager = manager.addListener(it, FactoryDiscoverable::class, it.action,
                    true, discoverableInfoFactory)
            }
        }

        if (control.debugEnabled) Log.d(TAG, "Started")
    }
}
