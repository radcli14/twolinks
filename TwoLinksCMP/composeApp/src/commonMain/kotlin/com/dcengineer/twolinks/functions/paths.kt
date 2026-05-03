package com.dcengineer.twolinks.functions

import com.dcengineer.twolinks.model.Planet


const val filesPath = "composeResources/twolinkscmp.composeapp.generated.resources/files"
const val environmentsPath = "$filesPath/environments"
const val modelsPath = "$filesPath/models"
fun fileLocation(planet: Planet): String {
    return resolveModelPath(planet.file)
}

fun resolveModelPath(modelName: String): String {
    return "$modelsPath/$modelName"
}

fun resolveEnvironmentPath(environmentName: String): String {
    return "$environmentsPath/$environmentName"
}