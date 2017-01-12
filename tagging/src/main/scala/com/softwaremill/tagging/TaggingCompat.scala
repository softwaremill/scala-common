package com.softwaremill.tagging

import scala.language.higherKinds
import scala.language.implicitConversions

trait TaggingCompat {

  implicit def liftTypeclass[Typeclass[_], T, Tag](implicit tc: Typeclass[T]): Typeclass[T @@ Tag] =
    tc.asInstanceOf[Typeclass[T @@ Tag]]

}

object TaggingCompat extends TaggingCompat
