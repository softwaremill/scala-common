package com.softwaremill.id.pretty

import org.scalatest.{FlatSpec, Matchers}

class DammSpec extends FlatSpec with Matchers {
  behavior of Damm.getClass.getSimpleName

  val max = Long.MaxValue

  it should "calculate check digit" in {
    val withChecksum = Damm(max.toString)

    Damm.isValid(withChecksum) should be(true)
  }

  it should "fail on checking check digit" in {
    val withChecksum = Damm(max.toString)

    (0 until withChecksum.length).foreach { i =>
      val sb      = new StringBuilder(withChecksum)
      val oldChar = sb.charAt(i).toString.toInt
      val newChar = ((oldChar + 1) % 10).toString
      sb.setCharAt(i, newChar.toCharArray.head)
      val corrupted = sb.toString()
      Damm.isValid(corrupted) should be(false)
    }
  }

}
