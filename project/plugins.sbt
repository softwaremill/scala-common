val sbtSoftwareMillVersion = "2.0.7"
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-common"  % sbtSoftwareMillVersion)
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-publish" % sbtSoftwareMillVersion)

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.6.0")

addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.8.0")
