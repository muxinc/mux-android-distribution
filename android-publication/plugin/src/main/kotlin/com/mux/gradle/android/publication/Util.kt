package com.mux.gradle.android.publication

import com.android.build.api.dsl.BuildType


fun variantNames(flavorValues: List<Set<String>>, buildTypes: Collection<BuildType>): List<String> {
  val names = mutableListOf<String>()
  flavorValues[0].forEach {
    names.addAll(variantNamesInner(flavorValues.subList(1, flavorValues.size), it, buildTypes))
  }

  return names
}

private fun variantNamesInner(
  flavorValues: List<Set<String>>,
  nameSoFar: String,
  buildTypes: Collection<BuildType>)
: List<String> {
  val names = mutableListOf<String>()
  if (flavorValues.isEmpty()) {
    // We got to the end so add the build types and return below
    buildTypes.onEach { buildType ->
      names.add(nameSoFar + capFirstLetter(buildType.name))
    }
  } else {
    flavorValues[0].forEach { variantName ->
      val flavorName = capFirstLetter(variantName)
      names.addAll(
        variantNamesInner(
          flavorValues.subList(1, flavorValues.size),
          nameSoFar + flavorName, buildTypes
        )
      )
    }
  }

  return names
}

fun capFirstLetter(string: String): String {
  return "${string[0].uppercase()}${string.substring(1)}"
}