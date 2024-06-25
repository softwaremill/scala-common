val sbtSoftwareMillVersion = "2.0.20"
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-common" % sbtSoftwareMillVersion)
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-publish" % sbtSoftwareMillVersion)

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.16.0")

addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.10.0")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.3")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.4")
