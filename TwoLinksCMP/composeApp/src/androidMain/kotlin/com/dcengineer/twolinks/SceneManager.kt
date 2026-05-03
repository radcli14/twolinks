package com.dcengineer.twolinks

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dcengineer.twolinks.functions.environmentsPath
import com.dcengineer.twolinks.functions.fileLocation
import com.dcengineer.twolinks.functions.resolveEnvironmentPath
import com.dcengineer.twolinks.model.Planet
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Filament
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.loaders.EnvironmentLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CameraNode
import io.github.sceneview.node.LightNode

class SceneManager(val context: Context) {
    val engine = Engine.create()

    val modelLoader = ModelLoader(engine, context)
    val environmentLoader = EnvironmentLoader(engine, context)
    /*val environment = environmentLoader.createHDREnvironment(
        assetFileLocation = resolveEnvironmentPath("NightSkyHDRI009_2K_HDR.hdr")
    ) ?: environmentLoader.createEnvironment()*/

    // Manage instances as state
    var moonInstance by mutableStateOf<ModelInstance?>(null)
    var earthInstance by mutableStateOf<ModelInstance?>(null)

    // Camera and Light are Nodes, so we can initialize them here
    val cameraNode = CameraNode(engine).apply {
        position = Float3(0f, 0f, 5f)
        updateProjection(far = 1000f)
    }

    private val entityManager = EntityManager.get()
    private val lightEntity = entityManager.create()
    val mainLightNode = LightNode(engine, lightEntity).apply {
        isShadowCaster = true
        rotation = Float3(x = 45f, y = 0f, z = 45f)
    }

    suspend fun loadModels(
        moonPath: String = fileLocation(Planet.moon),
        earthPath: String = fileLocation(Planet.earth)
    ) {
        moonInstance = modelLoader.loadModelInstance(moonPath)
        // Sequential loading to prevent GPU timeout
        if (moonInstance != null) {
            earthInstance = modelLoader.loadModelInstance(earthPath)
        }
    }

    fun onDestroy() {
        // Critical for memory management
        entityManager.destroy(lightEntity)
        engine.destroy()
    }
}