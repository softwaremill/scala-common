package com.softwaremill.benchmark

import scala.util.Random

object Timed {
  def timed[T](b: => T): (T, Long) = {
    val start = System.currentTimeMillis()
    val r = b
    (r, System.currentTimeMillis() - start)
  }

  def runTests(tests: List[(String, () => String)], repetitions: Int): Unit = {
    val allTests = Random.shuffle(List.fill(repetitions)(tests).flatten)

    println("Warmup")
    for ((name, body) <- tests) {
      val (result, time) = timed { body() }
      println(f"$name%-25s $result%-25s ${time / 1000.0d}%4.2fs")
    }

    println("---")
    println(s"Running ${allTests.size} tests")

    val rawResults = for ((name, body) <- allTests) yield {
      val (result, time) = timed { body() }
      println(f"$name%-25s $result%-25s ${time / 1000.0d}%4.2fs")
      name -> time
    }

    val results: Map[String, (Double, Double)] = rawResults.groupBy(_._1)
      .mapValues(_.map(_._2))
      .mapValues { times =>
        val count = times.size
        val mean = times.sum.toDouble / count
        val dev = times.map(t => (t - mean) * (t - mean))
        val stddev = Math.sqrt(dev.sum / count)
        (mean, stddev)
      }

    println("---")
    println("Averages (name,  mean, stddev)")
    results.toList.sortBy(_._2._1).foreach {
      case (name, (mean, stddev)) =>
        println(f"$name%-25s ${mean / 1000.0d}%4.2fs $stddev%4.2fms")
    }
  }
}