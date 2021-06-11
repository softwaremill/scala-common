package com.softwaremill.eitherops

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EitherOpsSpec extends AnyFlatSpec with Matchers {

  "collectLefts" should "find no left" in {
    val e1 = Right(1)
    val e2 = Right(2)
    val e3 = Right(3)

    EitherOps.collectLefts(e1, e2, e3) should be(Nil)
  }

  "collectLefts" should "find some lefts" in {
    val e1 = Right(1)
    val e2 = Left("nan")
    val e3 = Left("nan2")

    EitherOps.collectLefts(e1, e2, e3) should be(Seq("nan", "nan2"))
  }

  "collectLefts" should "find some lefts with mixed right types" in {
    val e1 = Right(1)
    val e2 = Right("2")
    val e3 = Left("nan")

    EitherOps.collectLefts(e1, e2, e3) should be(Seq("nan"))
  }

  "collectLefts" should "find some lefts with error trait" in {
    sealed trait Error
    case class NumericError(message: String) extends Error
    case class OtherError(message: String) extends Error

    val e1 = Right(1)
    val e2 = Left(NumericError("nan"))
    val e3 = Left(OtherError("foo"))

    val lefts: Seq[Error] = EitherOps.collectLefts(e1, e2, e3)
    lefts should be(Seq(NumericError("nan"), OtherError("foo")))
  }

  "collectRight" should "find no right" in {
    val e1 = Left(1)
    val e2 = Left(2)
    val e3 = Left(3)

    EitherOps.collectRights(e1, e2, e3) should be(Nil)
  }

  "collectRight" should "find some rights" in {
    val e0 = Left("nan")
    val e1 = Right(1)
    val e2 = Right(2)
    val e3 = Left("nan bis")
    val e4 = Right(3)

    EitherOps.collectRights(e0, e1, e2, e3, e4) should be(Seq(1, 2, 3))
  }
}
