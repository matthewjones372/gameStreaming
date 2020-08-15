name := "GamingStream"

version := "0.1"

scalaVersion := "2.13.3"

lazy val scalaTestVersion = "3.2.1"
lazy val refinedVersion = "0.9.15"
lazy val catsVersion = "2.0.0"
lazy val scalaCheckVersion = "1.14.3"
lazy val scalaCheckMagnoliaVersion = "0.4.0"
lazy val scalaCheckTestPlus = "3.1.0.0"

libraryDependencies ++= Seq(
  "eu.timepit" %% "refined" % refinedVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test,
  "com.github.chocpanda" %% "scalacheck-magnolia" % scalaCheckMagnoliaVersion % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % scalaCheckTestPlus % Test
)

scalafmtOnCompile := true