package com.mux.gradle.android.publication

class FlavorDimensionContainer: Iterable<Map.Entry<String, Set<String>>>{
  private val dimensions = mutableMapOf<String, MutableSet<String>>()

  fun addFlavor(dimension: String, flavor: String) {
    ensureKey(dimension)
    dimensions[dimension]?.add(flavor)
  }

  fun addEmptyDimension(dimension: String) {
    ensureKey(dimension)
  }

  fun getFlavorDimensions(): Set<String> {
    return dimensions.keys
  }

  fun getFlavors(forDimension: String): Set<String> {
    return dimensions[forDimension]!!
  }

  fun asMap(): Map<String, Set<String>> {
    return mutableMapOf<String, Set<String>>().also{ it.putAll(dimensions) }
  }

  private fun ensureKey(key: String) {
    if (!dimensions.containsKey(key)) {
      dimensions[key] = mutableSetOf()
    }
  }

  override fun iterator(): Iterator<Map.Entry<String, Set<String>>> = dimensions.iterator()
}
