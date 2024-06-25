import sbt._
import Keys._
import com.jsuereth.sbtpgp.PgpKeys.publishSigned
import sbt.Reference.display
import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import com.softwaremill.Publish.ossPublishSettings

val scala2_12 = "2.12.19"
val scala2_13 = "2.13.14"
val scala3 = "3.3.3"

val scala2Versions = List(scala2_12, scala2_13)
val scala2And3Versions = scala2Versions ++ List(scala3)

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.19"

lazy val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  organization := "com.softwaremill.common",
  parallelExecution := false,
  // Sonatype OSS deployment
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  Test / publishArtifact := false
)

val publishTagging = taskKey[Unit]("Publish the tagging projects; run sonatypeBundleRelease later")
val publishFutureTry = taskKey[Unit]("Publish the futureTry projects; run sonatypeBundleRelease later")
val publishFutureSquash = taskKey[Unit]("Publish the futureSquash projects; run sonatypeBundleRelease later")
val publishEitherOps = taskKey[Unit]("Publish the eitherOps projects; run sonatypeBundleRelease later")
val publishBenchmark = taskKey[Unit]("Publish the benchmark projects; run sonatypeBundleRelease later")

val enableMimaSettings = Seq(
  mimaPreviousArtifacts := {
    val current = version.value
    println(current)
    val isRcOrMilestone = current.contains("M") || current.contains("RC")
    if (!isRcOrMilestone) {
      val previous = previousStableVersion.value
      println(s"[info] Not a M or RC version, using previous version for MiMa check: $previous")
      previousStableVersion.value.map(organization.value %% moduleName.value % _).toSet
    } else {
      println(s"[info] $current is an M or RC version, no previous version to check with MiMa")
      Set.empty
    }
  }
)

val allAggregates =
  tagging.projectRefs ++ futureTry.projectRefs ++ futureSquash.projectRefs ++ eitherOps.projectRefs ++ benchmark.projectRefs
def filterProject(p: String => Boolean) = ScopeFilter(inProjects(allAggregates.filter(pr => p(display(pr.project))): _*))

lazy val scalaCommon = (project in file("."))
  .settings(commonSettings)
  .settings(
    publishArtifact := false,
    name := "scala-common",
    version := "1.0.0",
    scalaVersion := scala2_13,
    mimaPreviousArtifacts := Set.empty,
    publishTagging := publishSigned.all(filterProject(p => p.contains("tagging"))).value,
    publishFutureTry := publishSigned.all(filterProject(p => p.contains("futureTry"))).value,
    publishFutureSquash := publishSigned.all(filterProject(p => p.contains("futureSquash"))).value,
    publishEitherOps := publishSigned.all(filterProject(p => p.contains("eitherOps"))).value,
    publishBenchmark := publishSigned.all(filterProject(p => p.contains("benchmark"))).value
  )
  .aggregate(allAggregates: _*)

lazy val tagging = (projectMatrix in file("tagging"))
  .settings(commonSettings ++ enableMimaSettings)
  .settings(
    version := "2.3.5",
    name := "tagging"
  )
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)
  .nativePlatform(scalaVersions = scala2And3Versions)

lazy val futureTry = (projectMatrix in file("futureTry"))
  .settings(commonSettings ++ enableMimaSettings)
  .settings(
    version := "1.0.1",
    name := "futuretry",
    libraryDependencies += scalaTest
  )
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)

lazy val futureSquash = (projectMatrix in file("futureSquash"))
  .settings(commonSettings ++ enableMimaSettings)
  .settings(
    version := "1.0.1",
    name := "futureSquash",
    libraryDependencies += scalaTest
  )
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)

lazy val eitherOps = (projectMatrix in file("eitherOps"))
  .settings(commonSettings ++ enableMimaSettings)
  .settings(
    version := "1.0.1",
    name := "eitherOps",
    libraryDependencies += scalaTest
  )
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)

lazy val benchmark = (projectMatrix in file("benchmark"))
  .settings(commonSettings ++ enableMimaSettings)
  .settings(
    version := "1.0.1",
    name := "benchmark",
    libraryDependencies += scalaTest
  )
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)
