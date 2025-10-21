name := "programs-as-values"
version := "1.0.0"
scalaVersion := "3.3.6"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.13.0",
  "org.typelevel" %% "cats-effect" % "3.6.3",
  "co.fs2" %% "fs2-io" % "3.12.2"
)

fork := true
run / connectInput := true
