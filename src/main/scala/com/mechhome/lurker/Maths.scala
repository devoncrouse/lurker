package com.mechhome.lurker

object Maths {
  def normalize(value: Int, oldMin: Int, oldMax: Int, newMin: Int, newMax: Int): Int = {
    val oldRange = (oldMax - oldMin)
    val newRange = (newMax - newMin)
    val result = (((value - oldMin) * newRange) / oldRange) + newMin
    return result
  }
}