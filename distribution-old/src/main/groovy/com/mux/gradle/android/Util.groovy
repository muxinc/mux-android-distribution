package com.mux.gradle.android

class Util {

  static String capitalizeFirstLetter(String str) {
    return "${str.charAt(0).toUpperCase()}${str.substring(1)}"
  }
}
