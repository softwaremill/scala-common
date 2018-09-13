package com.softwaremill.id.pretty

import com.softwaremill.id.DefaultIdGenerator
import org.scalatest.{FlatSpec, Matchers}

class ExampleSpec extends FlatSpec with Matchers {

  it should "present example of usage" in {
    //create instance of it
    val generator:StringIdGenerator = new DefaultPrettyIdGenerator(new DefaultIdGenerator())

    //generate ids
    val stringId = generator.nextId()
    stringId shouldNot be(empty)
    stringId should fullyMatch regex """[A-Z]{4}-[0-9]{5}-[A-Z]{4}-[0-9]{5}"""
  }

}
