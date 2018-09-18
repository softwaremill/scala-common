package com.softwaremill.id.pretty

import com.softwaremill.id.IdGenerator
import org.scalatest.{FlatSpec, Matchers}

class ExampleSpec extends FlatSpec with Matchers {

  it should "present example of usage" in {
    //create instance of it
    val generator:StringIdGenerator = PrettyIdGenerator.singleNode

    //generate ids
    val stringId = generator.nextId()
    stringId shouldNot be(empty)
    stringId should fullyMatch regex """[A-Z]{4}-[0-9]{5}-[A-Z]{4}-[0-9]{5}"""

    //or it might be used just for encoding existing ids
    val prettifier = IdPrettifier.default
    val id = prettifier.prettify(100L) //id = AAAA-00000-AAAA-01007
    id should be("AAAA-00000-AAAA-01007")

    //get seed
    val origin = prettifier.toIdSeed(id) // 100L
    origin should be(Right(100L))

    //use custom prettifier
    val customPrettifier = IdPrettifier.custom(encoder = new AlphabetCodec(new Alphabet("ABC")), partsSize = 4, delimiter = '_', leadingZeros = false)
    val customId = customPrettifier.prettify(1234567L) //BCAACAB_5671

    //construct custom PrettyIdGenerator
    val idGenerator:IdGenerator = new IdGenerator {
      override def nextId(): Long = ???
      override def idBaseAt(timestamp: Long): Long = ???
    }
    val customGenerator = new PrettyIdGenerator(idGenerator, customPrettifier)
  }

}
