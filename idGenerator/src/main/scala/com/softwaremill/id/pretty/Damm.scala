package com.softwaremill.id.pretty

import scala.annotation.tailrec

/**
  * Implementation of Damm Check Digit alogrithm take from
  * http://en.wikipedia.org/wiki/Damm_algorithm
  * https://en.wikibooks.org/wiki/Algorithm_Implementation/Checksums/Damm_Algorithm
  */
object Damm {

  private val matrix = Array(
    Array(0, 3, 1, 7, 5, 9, 8, 6, 4, 2),
    Array(7, 0, 9, 2, 1, 5, 4, 8, 6, 3),
    Array(4, 2, 0, 6, 8, 7, 1, 3, 5, 9),
    Array(1, 7, 5, 0, 9, 8, 3, 4, 2, 6),
    Array(6, 1, 2, 3, 0, 4, 5, 9, 7, 8),
    Array(3, 6, 7, 4, 2, 0, 9, 5, 8, 1),
    Array(5, 8, 6, 9, 7, 2, 0, 1, 3, 4),
    Array(8, 9, 4, 5, 3, 6, 2, 0, 1, 7),
    Array(9, 4, 3, 8, 6, 1, 7, 2, 0, 5),
    Array(2, 5, 8, 1, 4, 3, 6, 7, 9, 0)
  )

  /**
    * Calculates the checksum from the provided string
    * @param str a string, only the numerics will be calculated
    */
  def encode(str: String): Int = {

    @tailrec
    def fn(interim: Int, idx: Int): Int =
      if (idx >= str.length) {
        interim
      } else {
        val c = str.charAt(idx)
        // only push numerics...
        fn(if (c.isDigit) matrix(interim)(c - 48) else interim, idx + 1)
      }

    fn(0, 0)
  }

  /**
    * Decorates the string with the checksum
    */
  def apply(str: String): String = str + encode(str).toString

  /**
    * Unapply method returning the string without the checksum if it matches otherwise None
    */
  def unapply(str: String): Option[String] =
    if (isValid(str)) Some(str.substring(0, str.length - 1)) else None

  /**
    * Determines if the string contains a valid checksum
    */
  def isValid(str: String): Boolean = encode(str) == 0

}
