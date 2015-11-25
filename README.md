# scala-common

[![Join the chat at https://gitter.im/softwaremill/scala-common](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/softwaremill/scala-common?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/softwaremill/scala-common.svg)](https://travis-ci.org/softwaremill/scala-common)

Tiny independent libraries with a single purpose, often a single class.

## Tagging

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/tagging_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/tagging_2.11)

Tag instances with arbitrary types. Useful if you'd like to differentiate between instances on the type level without
runtime overhead. Tags are only used at compile-time to provide additional type safety.

An instance of type `T` tagged with type `U` has type `T @@ U` (which is sugar for `@@[T, U]`). You can only use
`x: T @@ U` when an instance of type `T @@ V` is expected when `U` is a subtype of `V` (`U <: V`).

The tag can be any type, but usually it is just an empty marker trait.

To add tags to existing instances, you can use the `taggedWith[_]` method, which returns a tagged instance 
(`T.taggedWith[U]: T @@ U`). Tagged instances can be used as regular ones, without any constraints.
 
SBT dependency:

````scala
libraryDependencies += "com.softwaremill.common" %% "tagging" % "1.0.0"
````

Example:

````scala
import com.softwaremill.tagging._

class Berry()

trait Black
trait Blue

val berry = new Berry()
val blackBerry: Berry @@ Black = berry.taggedWith[Black]
val blueBerry: Berry @@ Blue = berry.taggedWith[Blue]

// compile error: val anotherBlackBerry: Berry @@ Black = blueBerry
````

Original idea by [Miles Sabin](https://gist.github.com/milessabin/89c9b47a91017973a35f).
Similar implementations are also available in [Shapeless](https://github.com/milessabin/shapeless) 
and [Scalaz](https://github.com/scalaz/scalaz).

## Id generator

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/id-generator_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/id-generator_2.11)

Generate unique ids. A default generator is provided, based on [Twitter Snowflake](https://github.com/twitter/snowflake),
which generates time-based ids.

SBT depedency:

````scala
libraryDependencies += "com.softwaremill.common" %% "id-generator" % "1.0.0"
````