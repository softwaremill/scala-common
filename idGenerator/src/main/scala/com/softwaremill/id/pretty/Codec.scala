package com.softwaremill.id.pretty

import scala.annotation.tailrec
import scala.math.pow

trait Codec {

  def encode(number: Long): String

  def decode(value: String): Long
}

class AlphabetCodec(alphabet: Alphabet) extends Codec {

  def encode(number: Long): String = encode(number, "")

  @tailrec
  private def encode(number: Long, rest: String): String = {
    val modulo: Int = (number % alphabet.base).toInt
    val result      = alphabet.valueOf(modulo).toString + rest
    if (number < alphabet.base) {
      result
    } else {
      encode(number / alphabet.base, result)
    }
  }

  def decode(value: String): Long = {
    case class ResultWithIndex(result: Long, index: Int)
    value
      .foldRight(ResultWithIndex(0, 0)) { (c, resultWithIndex) =>
        ResultWithIndex(
          resultWithIndex.result + alphabet.indexOf(c) * pow(alphabet.base, resultWithIndex.index).toInt,
          resultWithIndex.index + 1
        )
      }
      .result
  }
}

class Alphabet(private val values: String) {
  require(values.toSet.size == values.length)
  def base: Int = values.length

  def valueOf(i: Int): Char = values.charAt(i)

  def indexOf(i: Char): Int = values.indexOf(i)
}

object Alphabet {
  val Base23 = new Alphabet("ABCDEFGHJKLMNPQRSTUVXYZ")
}
