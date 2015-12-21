import sbt._
import Keys._

import scalariform.formatter.preferences._

val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
val scalaTest = "org.scalatest" %% "scalatest" % "2.2.5"

lazy val commonSettings = scalariformSettings ++ Seq(
  organization := "com.softwaremill.common",
  version := "1.0.0",

  scalaVersion := "2.11.7",

  scalacOptions ++= Seq("-unchecked", "-deprecation"),

  parallelExecution := false,

  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PreserveSpaceBeforeArguments, true)
    .setPreference(CompactControlReadability, true)
    .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
    .setPreference(SpacesAroundMultiImports, false),

  // Sonatype OSS deployment
  publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  credentials   += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra :=
    <scm>
      <url>git@github.com:adamw/macwire.git</url>
      <connection>scm:git:git@github.com:adamw/macwire.git</connection>
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
  .aggregate(tagging, idGenerator, futureTry)

lazy val tagging = (project in file("tagging"))
  .settings(commonSettings)
  .settings(
    version := "1.0.0",
    name := "tagging")

lazy val idGenerator = (project in file("idGenerator"))
  .settings(commonSettings)
  .settings(
    version := "1.0.0",
    name := "id-generator",
    libraryDependencies ++= Seq(scalaLogging, scalaTest))

lazy val futureTry = (project in file("futureTry"))
  .settings(commonSettings)
  .settings(
    version := "1.0.0",
    name := "futuretry",
    libraryDependencies += scalaTest)