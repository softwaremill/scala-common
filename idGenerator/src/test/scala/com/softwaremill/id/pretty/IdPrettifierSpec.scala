package com.softwaremill.id.pretty

import com.softwaremill.id.DefaultIdGenerator
import org.scalatest.{FlatSpec, Matchers}

class IdPrettifierSpec extends FlatSpec with Matchers {
  behavior of "IdPrettifier"

  val prettifier = new IdPrettifier()

  val max       = Long.MaxValue
  val exampleId = 824227036833910784L

  it should "generate pretty IDs with leading zeros" in {
    import prettifier._
    val maxPrettyId     = prettify(max)
    val examplePrettyId = prettify(exampleId)

    maxPrettyId should be("HPJD-72036-HAPK-58077")
    examplePrettyId should be("ARPJ-27036-GVQS-07849")
    prettify(1) should be("AAAA-00000-AAAA-00013")

    val prettifierBy8 = new IdPrettifier(partsSize = 8)
    prettifierBy8.prettify(1) should be("00000000-AAAAAA-00000013")
    prettifierBy8.prettify(max) should be("00009223-FTYTHN-47758077")
  }

  it should "generate pretty IDs without leading zeros" in {
    val prettifier      = new IdPrettifier(leadingZeros = false)
    val maxPrettyId     = prettifier.prettify(max)
    val examplePrettyId = prettifier.prettify(exampleId)

    maxPrettyId should be("HPJD-72036-HAPK-58077")
    examplePrettyId should be("RPJ-27036-GVQS-07849")
    prettifier.prettify(1) should be("13")

    val prettifierBy8 = new IdPrettifier(partsSize = 8, leadingZeros = false)
    prettifierBy8.prettify(1) should be("13")
    prettifierBy8.prettify(max) should be("9223-FTYTHN-47758077")

  }

  it should "find seed of pretty ID with leading zeros" in {
    val prettifiedWithLeading = new IdPrettifier()
    val maxPrettyId           = prettifiedWithLeading.prettify(max)
    val examplePrettyId       = prettifiedWithLeading.prettify(exampleId)

    prettifiedWithLeading.toIdSeed("HPJD-72036-HAPK-58077") should be(Left(max))
    prettifiedWithLeading.toIdSeed("ARPJ-27036-GVQS-07849") should be(Left(exampleId))
    prettifiedWithLeading.toIdSeed("AAAA-00000-AAAA-00013") should be(Left(1L))
  }

  it should "find seed of pretty ID without leading zeros" in {
    val prettifiedWithoutTrailingZeros = new IdPrettifier(leadingZeros = false)
    val maxPrettyId                    = prettifiedWithoutTrailingZeros.prettify(max)
    val examplePrettyId                = prettifiedWithoutTrailingZeros.prettify(exampleId)

    prettifiedWithoutTrailingZeros.toIdSeed("HPJD-72036-HAPK-58077") should be(Left(max))
    prettifiedWithoutTrailingZeros.toIdSeed("RPJ-27036-GVQS-07849") should be(Left(exampleId))
    prettifiedWithoutTrailingZeros.toIdSeed("13") should be(Left(1L))
  }

  it should "validate pretty IDs" in {
    import prettifier._
    isValid("HPJD-72036-HAPK-58077") should be(true)
    isValid("HPJD-72036-HAPK-58077") should be(true)
    isValid("ARPJ-27036-GVQS-07849") should be(true)
    isValid("ARPJ-27036-GVQS-07840") should be(false)
    isValid("ARPJ-27036-GVQS-07489") should be(false)
    isValid("ARPJ-27036-GVQZ-07489") should be(false)
  }

  it should "preserve ID monotonicity" in {
    import prettifier._
    val idGenerator = new DefaultIdGenerator()
    val ids = (1 to 100).map(_ => prettify(idGenerator.nextId()))
    ids.sorted should be(ids)
    ids.sorted.reverse should be(ids.reverse)
  }

  it should "keep same id length" in {
    import prettifier._
    val minId:String = prettify(0)
    val maxId:String = prettify(max)

    minId should have length maxId.length
  }

}
