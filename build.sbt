name := "GamingStream"

version := "0.1"

scalaVersion := "2.13.3"

lazy val scalaTestVersion = "3.2.1"
lazy val refinedVersion = "0.9.15"
lazy val catsVersion = "2.0.0"

libraryDependencies ++= Seq(
  "eu.timepit" %% "refined" % refinedVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)

scalafmtOnCompile := true