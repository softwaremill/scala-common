# scala-common

[![Join the chat at https://gitter.im/softwaremill/scala-common](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/softwaremill/scala-common?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![CI](https://github.com/softwaremill/scala-common/workflows/CI/badge.svg)](https://github.com/softwaremill/scala-common/actions?query=workflow%3A%22CI%22)

Tiny independent libraries with a single purpose, often a single class. Available for Scala 2.11, 2.12, 2.13, 3; JVM and JS.

## Tagging

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/tagging_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/tagging_2.13)

Tag instances with arbitrary types. Useful if you'd like to differentiate between instances on the type level without
runtime overhead. Tags are only used at compile-time to provide additional type safety.

An instance of type `T` tagged with type `U` has type `T @@ U` (which is sugar for `@@[T, U]`). You can only use
`x: T @@ U` when an instance of type `T @@ V` is expected when `U` is a subtype of `V` (`U <: V`).

The tag can be any type, but usually it is just an empty marker trait.

To add tags to existing instances, you can use the `taggedWith[_]` method, which returns a tagged instance 
(`T.taggedWith[U]: T @@ U`). Tagged instances can be used as regular ones, without any constraints.
 
SBT dependency:

````scala
libraryDependencies += "com.softwaremill.common" %% "tagging" % "2.3.3"
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

#### Tagging and typeclasses
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

val id: Long @@ UserId = 1024L.taggedWith[UserId]
serialize(id) // Won't compile: could not find implicit value for parameter ser
```
Because tagged type `T @@ U` is considered by the compiler as a different type than `T`, it will complain about missing implicit typeclass `Serializer[_]` for `T @@ U`, even if there is instance of `Serializer[T]` already in scope.

To solve this problem just either mix-in `TypeclassTaggingCompat[M[_]]`/`AnyTypeclassTaggingCompat` trait or import contents of the `AnyTypeclassTaggingCompat` object:
```scala
import com.softwaremill.tagging.AnyTypeclassTaggingCompat._

serialize(id) // Compiles and returns "Long number: 1024"
``` 

`TypeclassTaggingCompat` brings implicit conversion, that can adapt any implicit `M[T]` to be used as `M[T @@ U]`.

## Future Try extensions

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/futuretry_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/futuretry_2.13)

Provides two utility methods for extending `Future`:

 - `tried: Future[Try[T]]` - reifying the Future's result.
 - `transformTry(f: Try[T] => Try[S]): Future[S]` - corresponds to 2.12's new `transform` variant, allowing to supply a single function (if, for example, you already have one handy), 
 instead of two. _Note: unfortunately, it was not possible to name this method transform, due to how scalac handles implicit resolution._

SBT depedency:

````scala
libraryDependencies += "com.softwaremill.common" %% "futuretry" % "1.0.1"
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

## Future Squash
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/futuresquash_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/futuresquash_2.13)

### Goal

Monad stacks are not easy to compose (e.g. in a for comprehension), and if you don't want to use Monad transformers you can use this Future additional methods.
 
### FutureSquash.fromEither

```scala
import com.softwaremill.futuresquash.FutureSquash

FutureSquash.fromEither(Right("a")) //Future("a")
FutureSquash.fromEither(Left(BoomError)) //Future(BoomError)
```

### squash Future[Either[Throwable, A]] and Future[Try[A]]

You can use `squash` on a `Future[Either[Throwable, A]]` to get a `Future[A]`.

```scala
import FutureSquash._

abstract class Error(message: String) extends Exception(message)
case object BoomError extends Error("Boom")

val fea: Future[Either[Error, String]] = Future(Right("a"))
val feb: Future[Either[Error, String]] = Future(Left(BoomError))

fea.squash //Future("a")
feb.squash //Future(BoomError)
```

You can also `squash` on a `Future[Try[A]]` to get a `Future[A]` in much the same way:

```scala
import FutureSquash._

val fta: Future[Try[String]] = Future(Success("a"))
val ftb: Future[Try[String]] = Future(Failure(new Exception("Boom")))

fta.squash //Future("a")
ftb.squash //Future(Exception("Boom"))
```

It can also be useful to compose several `Future[Either[Throwable, _]]` without monad transformers :

```scala
def fea: Future[Either[Throwable, Int]] = Future(Right(1))
def feb(a: Int): Future[Either[Throwable, Int]] = Future(Right(a + 2))

val composedAB: Future[Int] = for {
  a <- fea.squash
  ab <- feb(a).squash
} yield ab

composedAB // Future("ab")

val error: Either[Throwable, Int] = Left(BoomError)
val composedABWithError: Future[Int] = for {
  a <- Future.successful(error).squash
  ab <- feb(a).squash
} yield ab

composedABWithError //Future(Failure(BoomError))

```

Composing several `Future[Try[_]]`s without monad transformers is also possible:

```scala
def fta: Future[Try[Int]] = Future(Success(1))
def ftb(a: Int): Future[Try[Int]] = Future(Success(a + 2))

val composedAB: Future[Int] = for {
  a <- fta.squash
  ab <- ftb(a).squash
} yield ab

composedAB // Future("ab")
```

### Options

Same operations can be used with options : `FutureSquash.fromOption` and `squash` on `Future[Option[A]]`.
For empty options, an `EmptyValueError` will be raised.

## Either additional operations (EitherOps)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/eitherops_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/eitherops_2.13)

This small util methods allow to use Either for multiple values validation, to avoid for comprehension fail-fast behavior and accumulate errors.

Example :

```scala
import com.softwaremill.eitherops._

case class Person(firstName: String, lastName: String, age: Int)

sealed trait Error
case class NumericError(message: String) extends Error
case class OtherError(message: String) extends Error

def validateAge(intValue: Int): Either[NumericError, Int] = ??? 
def validateFirstName(stringValue: String): Either[OtherError, String] = ???
def validateLastName(stringValue: String): Either[OtherError, String] = ???

val errors: Seq[Error] = EitherOps.collectLefts(
  validateFirstName("john"),
  validateLastName("doe"),
  validateAge(40)
)

if (errors.isEmpty) ???// use for comprehension here to build a Person 
else ??? // handle errors here
```

`EitherOpscollectRights` symmetric method is provided for convenience.
## Simple benchmarking utilities

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/futuretry_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/benchmarks_2.13)

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
