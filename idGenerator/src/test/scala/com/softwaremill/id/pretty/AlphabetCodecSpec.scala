package com.softwaremill.id.pretty

import org.scalatest.{FlatSpec, Matchers}

class AlphabetCodecSpec extends FlatSpec with Matchers {
  val codec = new AlphabetCodec(Alphabet.Base23)
  behavior of codec.getClass.getSimpleName

  val max       = Long.MaxValue
  val exampleId = 824227036833910784L

  it should "encode value" in {
    import codec._
    encode(23) should be("BA")
    encode(529) should be("BAA")
    encode(12167) should be("BAAA")
  }

  it should "decode value" in {
    import codec._
    decode("BA") should be(23)
    decode("ABA") should be(23)
    decode("BAA") should be(529)
    decode("BAB") should be(530)
    decode("BAAA") should be(12167)
    decode("HAPK") should be(85477)
    decode("HPJD") should be(92233)
  }
}
