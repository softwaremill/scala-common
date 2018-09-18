package com.softwaremill.id.pretty

import com.fasterxml.uuid.{EthernetAddress, Generators}
import com.softwaremill.id.DefaultIdGenerator
import org.scalatest.{FlatSpec, Matchers}

class IdPrettifierBenchmarkSpec extends FlatSpec with Matchers {
  behavior of "IdPrettifier"

  private def printResult(start: Long, stop: Long) = println(s"${(stop - start) / 1000.0} s")

  it should "measure performance of id prettifier" in {
    val idGenerator = new DefaultIdGenerator()
    val prettifier  = IdPrettifier.default
    val times       = 1 to 10000
    val start1      = System.currentTimeMillis()
    times.foreach { _ =>
      prettifier.prettify(idGenerator.nextId())
    }
    val stop1     = System.currentTimeMillis()
    val start2    = System.currentTimeMillis()
    val generator = Generators.timeBasedGenerator(EthernetAddress.fromInterface())
    times.foreach { _ =>
      generator.generate()
    }
    val stop2 = System.currentTimeMillis()
    println("Results for Prettifier:")
    printResult(start1, stop1)
    println("Results for UUID:")
    printResult(start2, stop2)
  }

  it should "measure performance of calculating seed ID" in {
    val idGenerator = new DefaultIdGenerator()
    val prettifier  = IdPrettifier.default
    val times       = 1 to 10000
    val seeds = times.map { _ =>
      val seed = idGenerator.nextId()
      (seed, prettifier.prettify(seed))
    }
    val start = System.currentTimeMillis()
    seeds.foreach { s =>
      val value = prettifier.toIdSeed(s._2)
      value should be(Right(s._1))
    }
    val stop = System.currentTimeMillis()
    println("Results for calculating seed:")
    printResult(start, stop)
  }


}
