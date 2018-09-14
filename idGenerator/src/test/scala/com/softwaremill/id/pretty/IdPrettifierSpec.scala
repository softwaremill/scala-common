package com.softwaremill.id.pretty

import com.softwaremill.id.DefaultIdGenerator
import org.scalatest.{FlatSpec, Matchers}

class IdPrettifierSpec extends FlatSpec with Matchers {
  behavior of "IdPrettifier"

  val max       = Long.MaxValue
  val exampleId = 824227036833910784L

  it should "generate pretty IDs with leading zeros" in {
    val default = IdPrettifier.default
    val maxPrettyId     = default.prettify(max)
    val examplePrettyId = default.prettify(exampleId)

    maxPrettyId should be("HPJD-72036-HAPK-58077")
    examplePrettyId should be("ARPJ-27036-GVQS-07849")
    default.prettify(1) should be("AAAA-00000-AAAA-00013")

    val prettifierBy8 = IdPrettifier.custom(partsSize = 8)
    prettifierBy8.prettify(1) should be("00000000-AAAAAA-00000013")
    prettifierBy8.prettify(max) should be("00009223-FTYTHN-47758077")
  }

  it should "generate pretty IDs without leading zeros" in {
    val prettifier      = IdPrettifier.custom(leadingZeros = false)
    val maxPrettyId     = prettifier.prettify(max)
    val examplePrettyId = prettifier.prettify(exampleId)

    maxPrettyId should be("HPJD-72036-HAPK-58077")
    examplePrettyId should be("RPJ-27036-GVQS-07849")
    prettifier.prettify(1) should be("13")

    val prettifierBy8 = IdPrettifier.custom(partsSize = 8, leadingZeros = false)
    prettifierBy8.prettify(1) should be("13")
    prettifierBy8.prettify(max) should be("9223-FTYTHN-47758077")

  }

  it should "find seed of pretty ID with leading zeros" in {
    val prettifiedWithLeading = IdPrettifier.default
    val maxPrettyId           = prettifiedWithLeading.prettify(max)
    val examplePrettyId       = prettifiedWithLeading.prettify(exampleId)

    prettifiedWithLeading.toIdSeed("HPJD-72036-HAPK-58077") should be(Right(max))
    prettifiedWithLeading.toIdSeed("ARPJ-27036-GVQS-07849") should be(Right(exampleId))
    prettifiedWithLeading.toIdSeed("AAAA-00000-AAAA-00013") should be(Right(1L))
  }

  it should "find seed of pretty ID without leading zeros" in {
    val prettifiedWithoutTrailingZeros = IdPrettifier.custom(leadingZeros = false)
    val maxPrettyId                    = prettifiedWithoutTrailingZeros.prettify(max)
    val examplePrettyId                = prettifiedWithoutTrailingZeros.prettify(exampleId)

    prettifiedWithoutTrailingZeros.toIdSeed("HPJD-72036-HAPK-58077") should be(Right(max))
    prettifiedWithoutTrailingZeros.toIdSeed("RPJ-27036-GVQS-07849") should be(Right(exampleId))
    prettifiedWithoutTrailingZeros.toIdSeed("13") should be(Right(1L))
  }

  it should "validate pretty IDs" in {
    import IdPrettifier.default._
    isValid("HPJD-72036-HAPK-58077") should be(true)
    isValid("HPJD-72036-HAPK-58077") should be(true)
    isValid("ARPJ-27036-GVQS-07849") should be(true)
    isValid("ARPJ-27036-GVQS-07840") should be(false)
    isValid("ARPJ-27036-GVQS-07489") should be(false)
    isValid("ARPJ-27036-GVQZ-07489") should be(false)
  }

  it should "preserve ID monotonicity" in {
    import IdPrettifier.default._
    val idGenerator = new DefaultIdGenerator()
    val ids         = (1 to 100).map(_ => prettify(idGenerator.nextId()))
    ids.sorted should be(ids)
    ids.sorted.reverse should be(ids.reverse)
  }

  it should "keep same id length" in {
    import IdPrettifier.default._
    val minId: String = prettify(0)
    val maxId: String = prettify(max)

    minId should have length maxId.length
  }

  it should "calculate seed properly - with default settings" in {
    val idGenerator = new DefaultIdGenerator()
    val prettifier = IdPrettifier.default

    val times = 1 to 10000
    times.map { _ =>
      val seed = idGenerator.nextId()
      val id = prettifier.prettify(seed)
      prettifier.toIdSeed(id) should be(Right(seed))
    }
  }

  it should "calculate seed properly - without leading zeros" in {
    val idGenerator = new DefaultIdGenerator()
    val prettifier = IdPrettifier.custom(leadingZeros = false)

    val times = 1 to 10000
    times.map { _ =>
      val seed = idGenerator.nextId()
      val id = prettifier.prettify(seed)
      val decodedSeed = prettifier.toIdSeed(id)
      decodedSeed should be(Right(seed))
    }

    times.map(_ => randomLong()).map { seed =>
      val id = prettifier.prettify(seed)
      val decodedSeed = prettifier.toIdSeed(id)
      decodedSeed should be(Right(seed))
    }
  }

  it should "calculate seed properly - without leading zeros and short alphabet" in {
    val idGenerator = new DefaultIdGenerator()
    val prettifier = IdPrettifier.custom(encoder = new AlphabetCodec(new Alphabet("ABC")), partsSize = 2)

    val times = 1 to 10000
    times.map { _ =>
      val seed = idGenerator.nextId()
      val id = prettifier.prettify(seed)
      val decodedSeed = prettifier.toIdSeed(id)
      decodedSeed should be(Right(seed))
    }

    times.map(_ => randomLong()).map { seed =>
      val id = prettifier.prettify(seed)
      val decodedSeed = prettifier.toIdSeed(id)
      decodedSeed should be(Right(seed))
    }
  }

  private def randomLong(): Long = {
    (Math.random() * Math.pow(10, 17)).toLong
  }
}
