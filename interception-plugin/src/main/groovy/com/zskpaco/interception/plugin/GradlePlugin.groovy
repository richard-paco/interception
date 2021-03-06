package com.zskpaco.interception.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.zskpaco.interception.plugin.bean.PluginConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.Opcodes

class GradlePlugin implements Plugin<Project> {

    public static boolean DEBUG = Version.DEBUG == "true"

    public static int OFFSET = 0

    @Override
    void apply(Project project) {
        if (!DEBUG) {
            OFFSET = Opcodes.ACC_SYNTHETIC
        }
        boolean isAndroidApplication = project.plugins.findPlugin("com.android.application") != null
        boolean isAndroidLibrary = project.plugins.findPlugin("com.android.library") != null
        if (isAndroidApplication || isAndroidLibrary) {
            if (!DEBUG) {
                project.dependencies.add("implementation", "com.github.zskpaco.interception:interception-core:${Version.VERSION}")
                boolean isKotlinProject = project.plugins.findPlugin("kotlin-android") != null
                if (isKotlinProject) {
                    project.dependencies.add("kapt", "com.github.zskpaco.interception:interception-processor:${Version.VERSION}")
                } else {
                    project.dependencies.add("annotationProcessor", "com.github.zskpaco.interception:interception-processor:${Version.VERSION}")
                }
            }

            project.extensions.create('interception', PluginConfig)
            //设置AnnotationProcessor需要的值
            DefaultConfig defaultConfig = project.android.defaultConfig
            Map<String, String> map = defaultConfig.javaCompileOptions.annotationProcessorOptions.arguments
            if (map != null) {
                map.put("moduleName", project.getName())
            }

            //注册
            if (isAndroidApplication) {
                AppExtension android = project.extensions.getByType(AppExtension)
                Transformer transformer = new Transformer(project)
                android.registerTransform(transformer)
            } else {
                LibraryExtension library = project.extensions.getByType(LibraryExtension)
                Transformer transformer = new Transformer(project)
                library.registerTransform(transformer)
            }
        } else {
            throw new IllegalArgumentException("Interception plugin must add in android application or library!")
        }

    }
}
