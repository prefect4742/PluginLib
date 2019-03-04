package com.prefect47.pluginlib

import com.google.auto.service.AutoService
import com.prefect47.pluginlib.PluginLibDependencies.Provider
import com.prefect47.pluginlib.plugin.annotations.Dependencies
import com.prefect47.pluginlib.plugin.annotations.DependsOn
import com.prefect47.pluginlib.plugin.annotations.ProvidesInterface
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

@AutoService(Processor::class)
class PluginAnnotationProcessor: AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val KAPT_OUTPACKAGE_OPTION = "pluginlib.generated.package"
        const val KAPT_OUTFILE_OPTION = "pluginlib.generated.classname"
    }

    private val providers = mutableSetOf<Element>()
    private val dependencies = mutableSetOf<Element>()
    private val multideps = mutableSetOf<Element>()

    override fun getSupportedAnnotationTypes() = mutableSetOf(
        ProvidesInterface::class.java.name,
        DependsOn::class.java.name,
        Dependencies::class.java.name
    )

    override fun getSupportedOptions() = mutableSetOf(
        KAPT_KOTLIN_GENERATED_OPTION_NAME,
        KAPT_OUTPACKAGE_OPTION,
        KAPT_OUTFILE_OPTION
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (!roundEnv.processingOver()) {

            val pkgName = processingEnv.options[KAPT_OUTPACKAGE_OPTION] ?: "com.prefect47.pluginlib"
            val clsName = processingEnv.options[KAPT_OUTFILE_OPTION] ?: "PluginLibDependenciesImpl"

            val properties = mutableSetOf<PropertySpec>()

            properties.add(parseProviders(roundEnv))
            properties.add(parseDependencies(roundEnv))

            // TODO: Could be a good idea to refer to classes by className instead of KClass<*>
            // TODO: That way the dependencies for loaded plugins can reside in their metadata in the manifest
            // TODO: Although when a plugin is started we will have to take the hit of reflection / class loading
            // TODO: at some point anyway, so maybe there's no point?
            // TODO: Also, the actual plugins only have a @Requires anyway - it should all be static lookups, right? :)
            // TODO: Wait and see how much performance improvement these static tables will give us when version
            // TODO: checking the built-in plugins first.

            val file = FileSpec.builder(pkgName, clsName)
                .addType(
                    TypeSpec.objectBuilder(clsName)
                        .addSuperinterface(PluginLibDependencies::class)
                        .addProperties(properties)
                        .build()
                )

            File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]).apply {
                mkdirs()
                file.build().writeTo(this)
            }
        }

        return false
    }

    private fun parseProviders(roundEnv: RoundEnvironment): PropertySpec {
        providers.addAll(roundEnv.getElementsAnnotatedWith(ProvidesInterface::class.java))
        val initializerCode =
            "mapOf(\n${providers.joinToString(",\n") { "%T::class to %T(%S, %L)" }}\n)"
        val initializerValues = providers.flatMap { listOf(
            it.asType().asTypeName(),
            Provider::class,
            it.getAnnotation(ProvidesInterface::class.java).action,
            it.getAnnotation(ProvidesInterface::class.java).version
        )}.toTypedArray()

        val type = Map::class.asTypeName().parameterizedBy(
            KClass::class.asTypeName().parameterizedBy(STAR), Provider::class.asTypeName()
        )
        return PropertySpec.builder("providers", type, KModifier.OVERRIDE)
            .initializer(initializerCode, *initializerValues)
            .build()
    }

    private fun getDependsType(a: DependsOn): TypeName {
        return try {
            a.target.asTypeName()
        } catch (e: MirroredTypeException) {
            e.typeMirror.asTypeName()
        }
    }

    private fun parseDependencies(roundEnv: RoundEnvironment): PropertySpec {
        dependencies.addAll(roundEnv.getElementsAnnotatedWith(DependsOn::class.java))
        multideps.addAll(roundEnv.getElementsAnnotatedWith(Dependencies::class.java))

        val deps = dependencies.groupBy ({it.asType().asTypeName()}, { it.getAnnotation(DependsOn::class.java) } )
        val mDeps = multideps.groupBy({it.asType().asTypeName()}, { it.getAnnotation(Dependencies::class.java).value })
            .mapValues { it.value.flatMap { it.asList() } }

        val sorted = deps.toMutableMap()
        sorted.putAll(mDeps)
        val initializerCode =
            "mapOf(\n${sorted.keys.joinToString(",\n") { "%T::class to listOf(${sorted[it]?.joinToString(", ") { "%T::class" }})" }}\n)"
        val initializerValues = sorted.entries.flatMap { entry ->
            listOf(entry.key) + entry.value.map { getDependsType(it) }
        }.toTypedArray()

        /*
        val sorted = dependencies.groupBy { it.asType().asTypeName() }
        val initializerCode =
            "mapOf(\n${sorted.keys.joinToString(",\n") { "%T::class to listOf(${sorted[it]?.joinToString(", ") { "%T::class" }})" }}\n)"
        val initializerValues = sorted.entries.flatMap { entry ->
            listOf(entry.key) + entry.value.map { getDependsType(it.getAnnotation(DependsOn::class.java)) }
        }.toTypedArray()
        */

        val type = Map::class.asTypeName().parameterizedBy(
            KClass::class.asTypeName().parameterizedBy(STAR),
            List::class.asTypeName().parameterizedBy(KClass::class.asTypeName().parameterizedBy(STAR))
        )
        return PropertySpec.builder("dependencies", type, KModifier.OVERRIDE)
            .initializer(initializerCode, *initializerValues)
            .build()
    }

}
