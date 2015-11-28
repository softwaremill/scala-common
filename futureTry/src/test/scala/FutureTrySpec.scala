import com.softwaremill.futuretry._
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, MustMatchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Await, Promise}
import scala.util.{Failure, Success, Try}

class FutureTrySpec extends FlatSpec with MustMatchers with TableDrivenPropertyChecks {

  import scala.concurrent.ExecutionContext.Implicits.global

  "tried" must "convert a successful result into a Success" in {
    val p = Promise[String]

    p.complete(Try("a"))

    Await.result(p.future.tried, Duration.Inf) must be(Success("a"))
  }

  it must "convert an exceptional result into a Failure" in {
    val p = Promise[String]
    val exception = new RuntimeException("blah")

    p.complete(Try(throw exception))

    Await.result(p.future.tried, Duration.Inf) must be(Failure(exception))
  }

  "transform" must "correctly transform between all Try variants in" in {
    val exception = new RuntimeException("bloh")

    val scenarios = Table[Try[String], Try[String] => Try[String], Try[String]] (
      ("original value", "transform", "expected output"),
      (Success("a"), identity[Try[String]], Success("a")),
      (Failure(exception), (x: Try[String]) => x match { case Failure(e) => Success(e.toString); case _ => ??? }, Success(exception.toString)),
      (Success("a"), (x: Try[String]) => x match { case Success(_) => Failure(exception); case _ => ??? }, Failure(exception)),
      (Failure(exception), identity[Try[String]], Failure(exception))
    )

    forAll(scenarios) {
      (orgValue, f, output) =>
        {
          val p = Promise[String]
          p.complete(orgValue)
          p.future.transTry(f) must haveResult(output)
        }
    }

  }

  def haveResult[A](value: Try[A]) = {
    be(value) compose { (f: Future[_]) => Await.result(f.tried, Duration.Inf) }
  }

}
