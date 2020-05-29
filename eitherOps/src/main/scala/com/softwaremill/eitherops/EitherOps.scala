package com.softwaremill.eitherops

object EitherOps {

  /**
    * Retrieves Left values for several Either values
    *  {{{
    *  case class Person(firstName: String, lastName: String, age: Int)
    *
    *  def validateAge(intValue: Int): Either[String, Int] = ???
    *  def validateFirstName(stringValue: String): Either[String, String] = ???
    *  def validateLastName(stringValue: String): Either[String, String] = ???
    *
    *  val errors: Seq[String] = EitherOps.collectLefts(
    *   validateFirstName("john"),
    *   validateLastName("doe"),
    *   validateAge(40)
    *  )
    *  if (errors.isEmpty) ???// use for comprehension here to build a Person
    *  else ??? // handle errors here
    *  }}}
    */
  def collectLefts[A](eithers: Either[A, _]*): Seq[A] = eithers.collect {
    case Left(a) => a
  }

  /** Retrieves Right values for several Either values
    */
  def collectRights[B](eithers : Either[_, B]*) : Seq[B]=  eithers.collect {
    case Right(b) => b
  }
}
