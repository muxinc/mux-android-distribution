package com.mux.gradle.android

/**
 * Maps a set of flavor dimensions to a set of product flavors that belong to them.
 */
class FlavorDimensionContainer implements Iterable<Map.Entry<String, Set<String>>> {

  private final Map<String, Set<String>> dimensions = new HashMap<>()

  /**
   * Add a flavor and dimension if they weren't already added
   */
  void addFlavor(String dimension, String flavor) {
    ensureKey(dimension)
    dimensions[dimension].add(flavor)
  }

  /**
   * Add a dimension with no flavors
   */
  void addEmptyDimension(String dimension) {
    ensureKey(dimension)
  }

  Set<String> getDimensions() {
    return dimensions.keySet().asUnmodifiable()
  }

  Set<String> getFlavors(String forDimension) {
    return dimensions[forDimension]
  }

  Map<String, Set<String>> asMap() {
    Map<String, Set<String>> deepCopy = new HashMap<>()
    dimensions.entrySet().each {
      def values = new HashSet(it.value)
      deepCopy.put(it.key, values)
    }
    return deepCopy
  }

  private void ensureKey(String dimension) {
    if (!dimensions.containsKey(dimension)) {
      dimensions.put(dimension, new HashSet<String>())
    }
  }

  @Override
  Iterator<Map.Entry<String, Set<String>>> iterator() {
    return dimensions.asUnmodifiable().iterator()
  }
}
