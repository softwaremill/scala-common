package com.softwaremill.tagging

trait TypeclassTaggingCompat[Typeclass[_]] {

  implicit def liftTypeclass[T, TTag](implicit tc: Typeclass[T]): Typeclass[T @@ TTag] =
    tc.asInstanceOf[Typeclass[T @@ TTag]]

}

trait AnyTypeclassTaggingCompat {

  implicit def liftAnyTypeclass[Typeclass[_], T, TTag](implicit tc: Typeclass[T]): Typeclass[T @@ TTag] =
    tc.asInstanceOf[Typeclass[T @@ TTag]]

}

object AnyTypeclassTaggingCompat extends AnyTypeclassTaggingCompat
