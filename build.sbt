import sbt._
import Keys._

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.7"

lazy val commonSettings = Seq(
  organization := "com.softwaremill.common",
  version := "1.0.0",

  scalaVersion := "2.11.12",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.8"),

  scalacOptions ++= Seq("-unchecked", "-deprecation"),

  parallelExecution := false,

  // Sonatype OSS deployment
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  credentials   += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
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
  licenses      := ("Apache2", new java.net.URL("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil,
  homepage      := Some(new java.net.URL("http://www.softwaremill.com"))
)

lazy val scalaCommon = (project in file("."))
  .settings(commonSettings)
  .settings(
    publishArtifact := false,
    name := "scala-common")
  .aggregate(tagging.jvm, tagging.js, futureTry, benchmark)

lazy val tagging = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("tagging"))
  .settings(commonSettings)
  .settings(
    version := "2.2.1",
    name := "tagging")

lazy val futureTry = (project in file("futureTry"))
  .settings(commonSettings)
  .settings(
    version := "1.0.0",
    name := "futuretry",
    libraryDependencies += scalaTest)

lazy val benchmark = (project in file("benchmark"))
  .settings(commonSettings)
  .settings(
    version := "1.0.0",
    name := "benchmark",
    libraryDependencies += scalaTest)
