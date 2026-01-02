ThisBuild / scalaVersion := "3.3.5"
ThisBuild / organization := "com.expatledger"
ThisBuild / organizationName := "The Expat Ledger"

val CatsEffectVersion = "3.5.7"
val Http4sVersion = "0.23.30"
val TapirVersion = "1.11.11"
val MunitVersion = "1.0.4"
val MunitCatsEffectVersion = "2.0.0"

lazy val root = (project in file("."))
  .aggregate(sharedKernel, apiGateway, tenantService)
  .settings(
    name := "expat-ledger-backend",
  )

lazy val sharedKernel = (project in file("modules/shared-kernel"))
  .enablePlugins(Fs2Grpc)
  .settings(
    name := "shared-kernel",
    Compile / scalacOptions ~= (_.filterNot(_ == "-Wvalue-discard")),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % MunitCatsEffectVersion % Test
    )
  )

lazy val apiGateway = (project in file("modules/api-gateway"))
  .dependsOn(sharedKernel)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "api-gateway",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % TapirVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % MunitCatsEffectVersion % Test
    )
  )

lazy val tenantService = (project in file("modules/tenant-service"))
  .dependsOn(sharedKernel)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "tenant-service",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % MunitCatsEffectVersion % Test
    )
  )
