package com.softwaremill

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

package object futuretry {

  implicit class FutureTry[T](val baseFuture: Future[T]) extends AnyVal {

    /** Reifies the Try output of Futures. Useful mostly for interaction with unusual APIs.
      * @return
      *   the same future with its result wrapped up in `Try`.
      */
    def tried(implicit executor: ExecutionContext): Future[Try[T]] = {
      baseFuture.map(Success.apply).recover { case f => Failure(f) }.mapTo[Try[T]]
    }

    /** @return
      *   a new future with the original's result applied by `f`.
      */
    def transformTry[S](f: Try[T] => Try[S])(implicit executor: ExecutionContext): Future[S] = {
      val p = Promise[S]
      baseFuture.onComplete(f.andThen(p.complete))
      p.future
    }

  }

}
