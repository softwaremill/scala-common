import sbt._
import Keys._

val scala2_11 = "2.11.12"
val scala2_12 = "2.12.14"
val scala2_13 = "2.13.6"
val scala3 = "3.0.0"

val scala2Versions = List(scala2_11, scala2_12, scala2_13)
val scala2And3Versions = scala2Versions ++ List(scala3)

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.9"

lazy val commonSettings = Seq(
  organization := "com.softwaremill.common",
  parallelExecution := false,
  // Sonatype OSS deployment
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ =>
    false
  },
  pomExtra :=
    <scm>
      <url>git@github.com:softwaremill/scala-common.git</url>
      <connection>scm:git:git@github.com:softwaremill/scala-common.git</connection>
    </scm>
      <developers>
        <developer>
          <id>adamw</id>
          <name>Adam Warski</name>
          <url>http://www.warski.org</url>
        </developer>
      </developers>,
  licenses := ("Apache2", new java.net.URL("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil,
  homepage := Some(new java.net.URL("http://www.softwaremill.com")),
  scalacOptions -= "-Xfatal-warnings"
)

lazy val scalaCommon = (project in file("."))
  .settings(commonSettings)
  .settings(publishArtifact := false, name := "scala-common", version := "1.0.0", scalaVersion := scala2_13)
  .aggregate(
    (tagging.projectRefs ++ futureTry.projectRefs ++ futureSquash.projectRefs ++ eitherOps.projectRefs ++ benchmark.projectRefs): _*
  )

lazy val tagging = (projectMatrix in file("tagging"))
  .settings(commonSettings)
  .settings(version := "2.3.0", name := "tagging")
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)

lazy val futureTry = (projectMatrix in file("futureTry"))
  .settings(commonSettings)
  .settings(version := "1.0.0", name := "futuretry", libraryDependencies += scalaTest)
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)

lazy val futureSquash = (projectMatrix in file("futureSquash"))
  .settings(commonSettings)
  .settings(version := "1.0.0", name := "futureSquash", libraryDependencies += scalaTest)
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)

lazy val eitherOps = (projectMatrix in file("eitherOps"))
  .settings(commonSettings)
  .settings(version := "1.0.0", name := "eitherOps", libraryDependencies += scalaTest)
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)

lazy val benchmark = (projectMatrix in file("benchmark"))
  .settings(commonSettings)
  .settings(version := "1.0.0", name := "benchmark", libraryDependencies += scalaTest)
  .jvmPlatform(scalaVersions = scala2And3Versions)
  .jsPlatform(scalaVersions = scala2And3Versions)
