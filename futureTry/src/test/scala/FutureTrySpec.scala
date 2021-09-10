import com.softwaremill.futuretry._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Await, Promise}
import scala.util.{Failure, Success, Try}

class FutureTrySpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks with ScalaFutures {

  import scala.concurrent.ExecutionContext.Implicits.global

  "tried" must "convert a successful result into a Success" in {
    val p = Promise[String]
    p.complete(Try("a"))

    val transformedFuture = p.future.tried

    transformedFuture.futureValue must be(Success("a"))
  }

  it must "convert an exceptional result into a Failure" in {
    val p = Promise[String]
    val exception = new RuntimeException("blah")
    p.complete(Try(throw exception))

    val transformedFuture = p.future.tried

    transformedFuture.futureValue must be(Failure(exception))
  }

  "transform" must "correctly transform between all Try variants in" in {
    val exception = new RuntimeException("bloh")

    val scenarios = Table[Try[String], Try[String] => Try[String], Try[String]](
      ("original value", "transform", "expected output"),
      (Success("a"), identity[Try[String]], Success("a")),
      (
        Failure(exception),
        (x: Try[String]) => x match { case Failure(e) => Success(e.toString); case _ => ??? },
        Success(exception.toString)
      ),
      (Success("a"), (x: Try[String]) => x match { case Success(_) => Failure(exception); case _ => ??? }, Failure(exception)),
      (Failure(exception), identity[Try[String]], Failure(exception))
    )

    forAll(scenarios) { (orgValue, f, output) =>
      {
        val p = Promise[String]
        p.complete(orgValue)

        val transformedFuture = p.future.transformTry(f)

        transformedFuture.tried.futureValue must be(output)
      }
    }
  }

}
