name := "GamingStream"

version := "0.1"

scalaVersion := "2.13.12"

lazy val refinedVersion = "0.11.1"
lazy val catsVersion = "2.0.0"
lazy val scalaLoggingVersion = "3.9.5"
lazy val logbackVersion = "1.4.12"

//Test dependencies
lazy val scalaTestVersion = "3.2.19"
lazy val scalaCheckVersion = "1.18.0"
lazy val scalaCheckMagnoliaVersion = "0.6.0"
lazy val scalaCheckTestPlus = "3.2.2.0"

libraryDependencies ++= Seq(
  "eu.timepit" %% "refined" % refinedVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test,
  "com.github.chocpanda" %% "scalacheck-magnolia" % scalaCheckMagnoliaVersion % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % scalaCheckTestPlus % Test
)
