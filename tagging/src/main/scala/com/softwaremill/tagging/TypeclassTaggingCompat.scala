package com.softwaremill.tagging

import scala.language.{higherKinds, implicitConversions}

trait TypeclassTaggingCompat[Typeclass[_]] {

  implicit def liftTypeclass[T, Tag](implicit tc: Typeclass[T]): Typeclass[T @@ Tag] =
    tc.asInstanceOf[Typeclass[T @@ Tag]]

}

trait AnyTypeclassTaggingCompat {

  implicit def liftAnyTypeclass[Typeclass[_], T, Tag](implicit tc: Typeclass[T]): Typeclass[T @@ Tag] =
    tc.asInstanceOf[Typeclass[T @@ Tag]]

}

object AnyTypeclassTaggingCompat extends AnyTypeclassTaggingCompat
