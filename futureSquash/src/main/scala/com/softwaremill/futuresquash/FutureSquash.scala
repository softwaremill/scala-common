package com.softwaremill.futuresquash

import scala.concurrent.{ExecutionContext, Future}
import scala.util._

object FutureSquash {

  /** Converts an Either[Throwable, A] to a Future[A] that may raise a Throwable
    */
  def fromEither[A](either: Either[Throwable, A]): Future[A] = {
    either match {
      case Right(a)    => Future.successful(a)
      case Left(error) => Future.failed(error)
    }
  }

  implicit class FutureEither[A](futureEitherStack: Future[Either[Throwable, A]]) {
    def squash(implicit ec: ExecutionContext): Future[A] = futureEitherStack.flatMap(fromEither)
  }

  implicit class FutureTry[A](futureTryStack: Future[Try[A]]) {
    def squash(implicit ec: ExecutionContext): Future[A] = futureTryStack.flatMap(Future.fromTry)
  }

  /** Converts an Option[A] to a Future[A] that may raise a NoSuchElementException
    */
  def fromOption[A](option: Option[A]): Future[A] = option match {
    case Some(a) => Future.successful(a)
    case None    => Future.failed(new NoSuchElementException)
  }

  implicit class FutureOption[A](futureOptionStack: Future[Option[A]]) {
    def squash(implicit ec: ExecutionContext): Future[A] = futureOptionStack.flatMap(fromOption)
  }

}
