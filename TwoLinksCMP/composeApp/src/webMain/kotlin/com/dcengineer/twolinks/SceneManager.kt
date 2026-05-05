package com.dcengineer.twolinks

import com.dcengineer.twolinks.functions.fileLocation
import com.dcengineer.twolinks.functions.resolveEnvironmentPath
import com.dcengineer.twolinks.model.Planet

class SceneManager {
    // Prepend "./" for browser fetch() — Android's AssetManager doesn't need it
    val moonPath = "./${fileLocation(Planet.moon)}"
    val earthPath = "./${fileLocation(Planet.earth)}"
    val environmentPath = "./${resolveEnvironmentPath("NightSkyHDRI008_4K_HDR_ibl.ktx")}"
    val skyboxPath = "./${resolveEnvironmentPath("NightSkyHDRI008_4K_HDR_skybox.ktx")}"

}