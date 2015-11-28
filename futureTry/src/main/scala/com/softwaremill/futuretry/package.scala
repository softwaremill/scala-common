package com.softwaremill

import scala.concurrent.{Promise, ExecutionContext, Future}
import scala.util.{Success, Failure, Try}

package object futuretry {

  implicit class FutureTry[T](private val baseFuture: Future[T]) extends AnyVal {

    /**
      * Reifies the Try output of Futures. Useful mostly for interaction with unusual APIs.
      * @return the same future with its result wrapped up in `Try`.
      */
    def tried(implicit executor: ExecutionContext): Future[Try[T]] = {
      baseFuture.map(Success.apply).recover(PartialFunction(Failure.apply)).mapTo[Try[T]]
    }

    /**
      * @return a new future with the original's result applied by `f`.
      */
    def transTry[S](f: Try[T] => Try[S])(implicit executor: ExecutionContext): Future[S] = {
      val p = Promise[S]
      baseFuture.onComplete(f.andThen(p.complete))
      p.future
    }

  }

}
