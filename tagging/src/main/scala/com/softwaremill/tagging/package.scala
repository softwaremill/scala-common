package com.softwaremill

/** Tag instances with arbitrary types. The tags are usually empty `trait`s. Tags have no runtime overhead and are only used at compile-time
  * for additional type safety.
  *
  * For example:
  *
  * {{{
  *   class Berry()
  *
  *   trait Black
  *   trait Blue
  *
  *   val berry = new Berry()
  *   val blackBerry: Berry @@ Black = berry.taggedWith[Black]
  *   val blueBerry: Berry @@ Blue = berry.taggedWith[Blue]
  *
  *   // compile error: val anotherBlackBerry: Berry @@ Black = blueBerry
  * }}}
  *
  * Original idea by Miles Sabin, see: https://gist.github.com/milessabin/89c9b47a91017973a35f
  */
package object tagging {
  trait Tag[+U] extends Any { type Tag <: U }
  type @@[+T, +U] = T with Tag[U]
  type Tagged[+T, +U] = T with Tag[U]

  implicit class Tagger[T](val t: T) extends AnyVal {
    @inline def taggedWith[U]: T @@ U = t.asInstanceOf[T @@ U]
  }
  implicit class AndTagger[T, U](val t: T @@ U) extends AnyVal {
    @inline def andTaggedWith[V]: T @@ (U with V) = t.asInstanceOf[T @@ (U with V)]

    @inline def replaceTag[V]: T @@ V = t.asInstanceOf[T @@ V]
    @inline def eraseTag: T = t.asInstanceOf[T]
  }

  implicit class TaggingF[F[_], T](val fa: F[T]) extends AnyVal {
    @inline def taggedWithF[B]: F[T @@ B] = fa.asInstanceOf[F[T @@ B]]
  }

  implicit class AndTaggingF[F[_], T, U](val ft: F[T @@ U]) extends AnyVal {
    @inline def eraseTagF: F[T] = ft.asInstanceOf[F[T]]

    @inline def andTaggedWithF[V]: F[T @@ (U with V)] = ft.asInstanceOf[F[T @@ (U with V)]]
    @inline def replaceTag[V]: F[T @@ V] = ft.asInstanceOf[F[T @@ V]]
  }

  implicit class TaggingF2[F1[_], F2[_], T](val ft: F1[F2[T]]) extends AnyVal {
    @inline def taggedWithF2[B]: F1[F2[T @@ B]] = ft.asInstanceOf[F1[F2[T @@ B]]]
  }

  implicit class AndTaggingF2[F1[_], F2[_], T, U](val ft: F1[F2[T @@ U]]) extends AnyVal {
    @inline def eraseTagF2: F1[F2[T]] = ft.asInstanceOf[F1[F2[T]]]

    @inline def replaceTagF2[V]: F1[F2[T @@ V]] = ft.asInstanceOf[F1[F2[T @@ V]]]
    @inline def andTaggedWithF2[V]: F1[F2[T @@ (U with V)]] = ft.asInstanceOf[F1[F2[T @@ (U with V)]]]
  }
}
