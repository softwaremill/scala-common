package com.softwaremill.id
import org.scalatest.{MustMatchers, WordSpec}

class DefaultIdGeneratorSpec extends WordSpec with MustMatchers {
  val workerMask = 0x000000000001F000L
  val datacenterMask = 0x00000000003E0000L
  val timestampMask = 0xFFFFFFFFFFC00000L

  class EasyTimeWorker(workerId: Long, datacenterId: Long, timeStart: Long = System.currentTimeMillis()) extends IdWorker(workerId, datacenterId) {
    var timeMaker = () => timeStart
    override def timeGen(): Long = {
      timeMaker()
    }
  }

  class WakingIdWorker(workerId: Long, datacenterId: Long)
      extends EasyTimeWorker(workerId, datacenterId) {
    var slept = 0
    override def tilNextMillis(lastTimestamp: Long): Long = {
      slept += 1
      super.tilNextMillis(lastTimestamp)
    }
  }

  "IdWorker" should {

    "generate an id" in {
      val s = new IdWorker(1, 1)
      val id: Long = s.nextId()
      id must be > 0L
    }

    "return an accurate timestamp" in {
      val s = new IdWorker(1, 1)
      val t = System.currentTimeMillis
      (s.get_timestamp() - t) must be < 50L
    }

    "return the correct job id" in {
      val s = new IdWorker(1, 1)
      s.get_worker_id() must be(1L)
    }

    "return the correct dc id" in {
      val s = new IdWorker(1, 1)
      s.get_datacenter_id() must be(1L)
    }

    "properly mask worker id" in {
      val workerId = 0x1F
      val datacenterId = 0
      val worker = new IdWorker(workerId, datacenterId)
      for (i <- 1 to 1000) {
        val id = worker.nextId
        ((id & workerMask) >> 12) must be(workerId)
      }
    }

    "properly mask dc id" in {
      val workerId = 0
      val datacenterId = 0x1F
      val worker = new IdWorker(workerId, datacenterId)
      val id = worker.nextId
      ((id & datacenterMask) >> 17) must be(datacenterId)
    }

    "properly mask timestamp" in {
      val worker = new EasyTimeWorker(31, 31)
      for (i <- 1 to 100) {
        val t = System.currentTimeMillis
        worker.timeMaker = () => t
        val id = worker.nextId
        ((id & timestampMask) >> 22) must be(t - worker.twepoch)
      }
    }

    "roll over sequence id" in {
      // put a zero in the low bit so we can detect overflow from the sequence
      val workerId = 4
      val datacenterId = 4
      val worker = new IdWorker(workerId, datacenterId)
      val startSequence = 0xFFFFFF - 20
      val endSequence = 0xFFFFFF + 20
      worker.sequence = startSequence

      for (i <- startSequence to endSequence) {
        val id = worker.nextId
        ((id & workerMask) >> 12) must be(workerId)
      }
    }

    "generate increasing ids" in {
      val worker = new IdWorker(1, 1)
      var lastId = 0L
      for (i <- 1 to 100) {
        val id = worker.nextId
        id must be > lastId
        lastId = id
      }
    }

    "generate 1 million ids quickly" in {
      val worker = new IdWorker(31, 31)
      val t = System.currentTimeMillis
      for (i <- 1 to 1000000) {
        var id = worker.nextId
        id
      }
      val t2 = System.currentTimeMillis
      println("generated 1000000 ids in %d ms, or %,.0f ids/second".format(t2 - t, 1000000000.0 / (t2 - t)))
      1 must be > 0
    }

    "sleep if we would rollover twice in the same millisecond" in {
      var queue = new scala.collection.mutable.Queue[Long]()
      val worker = new WakingIdWorker(1, 1)
      val iter = List(2L, 2L, 3L).iterator
      worker.timeMaker = () => iter.next
      worker.sequence = 4095
      worker.nextId
      worker.sequence = 4095
      worker.nextId
      worker.slept must be(1)
    }

    "generate only unique ids" in {
      val worker = new IdWorker(31, 31)
      var set = new scala.collection.mutable.HashSet[Long]()
      val n = 2000000
      (1 to n).foreach { i =>
        val id = worker.nextId
        if (set.contains(id)) {
          println(java.lang.Long.toString(id, 2))
        }
        else {
          set += id
        }
      }
      set.size must be(n)
    }

    "generate ids over 50 billion" in {
      val worker = new IdWorker(0, 0)
      worker.nextId must be > 50000000000L
    }

    "generate ids older then lower bound" in {
      //given
      val worker = new IdWorker(0, 0)
      val lowerBound = worker.idForTimestamp(System.currentTimeMillis())

      //when
      val ids = List(worker.nextId, worker.nextId, worker.nextId)

      //then
      ids.foreach(_ >= lowerBound must be(true))
    }

    "generate older lowerBound then next generated ids from distinct workers" in {
      //given
      val worker = new IdWorker(0, 0)
      val lowerBound = worker.idForTimestamp(System.currentTimeMillis())

      //when
      val ids = List(
        new DefaultIdGenerator(workerId = 1).nextId,
        new DefaultIdGenerator(workerId = 2).nextId,
        new DefaultIdGenerator(workerId = 3).nextId
      )

      //then
      ids.foreach(_ >= lowerBound must be(true))
    }

    "generate range of ids that catches only 3 oldest ids" in {
      //given
      val currentPoint = System.currentTimeMillis()
      val upperBoundOverheadAndExtraTime = 300000 // (datacenterId << datacenterIdShift) | (workerId << workerIdShift)

      val gen = new EasyTimeWorker(0, 1, timeStart = currentPoint)
      val lowerBound = gen.idForTimestamp(currentPoint)
      val upperBound = lowerBound + upperBoundOverheadAndExtraTime

      val ids = List(gen.nextId, gen.nextId, gen.nextId)

      //when
      val laterMilis = 120
      val olderIds = new EasyTimeWorker(1, 2, timeStart = currentPoint + laterMilis)
      val newList = ids ++ List(olderIds.nextId, olderIds.nextId, olderIds.nextId)

      //then
      newList.count(id => id >= lowerBound && id < upperBound) must be (3)
    }
  }
}
