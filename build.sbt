name := "movie-ticket-reservation"

version := "1.0"

scalaVersion := "2.12.2"

scalacOptions := Seq("-encoding", "utf8")


libraryDependencies ++= akka ++ database

lazy val akka = {
  val akkaVersion = "2.4.18"
  val akkaHttpVersion = "10.0.6"
  val scalaTestVersion = "3.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test
  )
}

lazy val database = {
  Seq(
      "com.github.etaty" %% "rediscala" % "1.8.0"
  )
}