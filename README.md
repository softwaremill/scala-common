# scala-common

[![Build Status](https://travis-ci.org/softwaremill/scala-common.svg)](https://travis-ci.org/softwaremill/scala-common)
[![Join the chat at https://gitter.im/softwaremill/scala-common](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/softwaremill/scala-common?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

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

####Tagging and typeclasses
Let's consider the following example:

```scala
import com.softwaremill.tagging._

// Our typeclass
trait Serializer[T] {
  def doSerialize(t: T): String
}

// Method that leverages typeclass, to transform some T into a String
def serialize[T](t: T)(implicit ser: Serializer[T]): String = {
  ser.doSerialize(t)
}

// Typeclass instance for type `Long`
implicit val longSerializer = new Serializer[Long] {
  override def doSerialize(t: Long): String = "Long number: " + t
}

val longNumber = 30L
serialize(longNumber) // Compiles and returns "Long number: 30"

// Our marker trait to be used as a tag
trait UserId

val id: Long @@ Id = 1024L.taggedWith[UserId]
serialize(id) // Won't compile: could not find implicit value for parameter ser
```
Because tagged type `T @@ U` is considered by the compiler as a different type than `T`, it will complain about missing implicit typeclass `Serializer[_]` for `T @@ U`, even if there is instance of `Serializer[T]` already in scope.

To solve this problem just either mix-in `TaggingCompat` trait or import contents of its companion object:
```scala
import com.softwaremill.tagging.TaggingCompat._

serialize(id) // Compiles and returns "Long number: 1024"
``` 

`TaggingCompat` brings implicit conversion, that can adapt any implicit `M[T]` to be used as `M[T @@ U]`.


## Id generator

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/id-generator_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/id-generator_2.11)

Generate unique ids. A default generator is provided, based on [Twitter Snowflake](https://github.com/twitter/snowflake),
which generates time-based ids.

SBT depedency:

````scala
libraryDependencies += "com.softwaremill.common" %% "id-generator" % "1.1.0"
````

## Future Try extensions

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/futuretry_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/futuretry_2.11)

Provides two utility methods for extending `Future`:

 - `tried: Future[Try[T]]` - reifying the Future's result.
 - `transformTry(f: Try[T] => Try[S]): Future[S]` - corresponds to 2.12's new `transform` variant, allowing to supply a single function (if, for example, you already have one handy), 
 instead of two. _Note: unfortunately, it was not possible to name this method transform, due to how scalac handles implicit resolution._

SBT depedency:

````scala
libraryDependencies += "com.softwaremill.common" %% "futuretry" % "1.0.0"
````

Example:

````scala

val myFuture: Future[Foo] = ...
val myUsefulTransformer: Try[Foo] => Try[Bar] = ...

def someWeirdApiMethod(future: Future[Try[Foo]])
 
import com.softwaremill.futuretry._

someWeirdApiMethod(myFuture.tried)

val myBetterFuture: Future[Bar] = myFuture.transformTry(myUsefulTransformer)
````

## Simple benchmarking utilities

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/futuretry_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/benchmarks_2.11)

Provides utilities for benchmarking.

 - `Timed.runTests(tests: List[(String, () => String)], repetitions: Int)`: runs specified number of repetitions of
  given code blocks and collects results (*mean* and *standard deviation*). A warmup round of all tests will be executed
  before measuring any statistics. Tests will be executed in random order.
 
 - `Timed.runTests(tests, repetitions, warmup)` runs multiple repetitions of shuffled tests provided as a list of `PerfTest` 
 instances. Each `PerfTest` should define a name, body (synchronous code block) and, optionally, an additional code block 
 that specifies a "warmup" that will be run before each test execution. The `Timed` object can also consume an optional 
 `warmup` argument which specified code block that should be executed before all tests. If omitted, the default global warmup 
 will run all the provided tests once (without collecting metrics).
   
Example:

````scala

val simpleTests: List[(String, () => String)] = List(
  ("test1", () => {
    // do some calculation
    "Ok"
  }),
  ("test2", () => {
    // do some other calculation
    "Ok"
  })
)

Timed.runTests(simpleTests, repetitions = 50)
````
or
````scala

case class MyTest(name: String, param: Int) extends PerfTest {

  override def warmup(): Unit = {
    // prepare some resources
  }

  override def run(): Try[String] = {
    // do some calculations
    Success(String)
  }
}
  
val tests: List[MyTest] = List(
  MyTest("test1", param = 665), MyTest("test2", param = 777)    
)
  
Timed.runTests(tests, repetitions = 50, warmup = (tests: List[MyTest]) => {
  // global warmup body
})
````
