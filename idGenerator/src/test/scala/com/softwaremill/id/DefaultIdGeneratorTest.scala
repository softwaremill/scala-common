package com.softwaremill.id
import org.scalatest.{FunSuite, Matchers}

class DefaultIdGeneratorTest extends FunSuite with Matchers {
  val generator = new DefaultIdGenerator()

  class EasyTimeWorker(workerId: Long = 2, datacenterId: Long = 2, timeStart: Long) extends IdWorker(workerId, datacenterId) {
    override def timeGen(): Long = timeStart
  }

  test("Three ids should be older then lower bound") {
    //given
    val lowerBound = generator.idFrom(System.currentTimeMillis())

    //when
    val ids = List(generator.nextId(), generator.nextId(), generator.nextId())

    //then
    ids.foreach(_ >= lowerBound shouldBe true)
  }

  test("Ids from distinct workers should be in a range") {
    //given
    val lowerBound = generator.idFrom(System.currentTimeMillis())

    //when
    val ids = List(generator.nextId(), new DefaultIdGenerator(workerId = 2).nextId(), new DefaultIdGenerator(workerId = 3).nextId())

    //then
    ids.foreach(_ >= lowerBound shouldBe true)
  }

  test("Only first 3 ids in a range") {
    //given
    val currentPoint = System.currentTimeMillis()
    val upperBoundOverheadAndExtraTime = 300000 // (datacenterId << datacenterIdShift) | (workerId << workerIdShift)

    val gen = new EasyTimeWorker(timeStart = currentPoint)
    val lowerBound = gen.idForTimestamp(currentPoint)
    val upperBound = lowerBound + upperBoundOverheadAndExtraTime

    val ids = List(gen.nextId(), gen.nextId(), gen.nextId())

    //when
    val laterMilis = 120
    val olderIds = new EasyTimeWorker(timeStart = currentPoint + laterMilis)
    val newList = ids ++ List(olderIds.nextId(), olderIds.nextId(), olderIds.nextId())

    //then
    newList.count(id => id >= lowerBound && id < upperBound) shouldBe 3
  }

}