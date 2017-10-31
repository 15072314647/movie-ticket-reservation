name := "movie-ticket-reservation"

version := "1.0"

scalaVersion := "2.12.4"

scalacOptions := Seq("-encoding", "utf8")


libraryDependencies ++= cats ++ akka ++ database


lazy val cats = {
  val catsVersion = "1.0.0"
  Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-free" % catsVersion
  )
}

lazy val akka = {
  val akkaVersion = "2.5.10"
  val akkaHttpVersion = "10.0.10"
  val scalaTestVersion = "3.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
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