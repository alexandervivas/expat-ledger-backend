import sbt.*

object Dependencies {
  object Versions {
    val CatsEffect = "3.6.3"
    val Http4s     = "0.23.33"
    val Tapir      = "1.13.4"
    val Munit      = "1.2.1"
    val MunitCatsEffect = "2.1.0"
    val Ip4s        = "3.7.0"
    val Grpc        = "1.78.0"
    val Skunk       = "0.6.5"
    val Flyway      = "11.20.0"
    val Postgresql  = "42.7.8"
    val PureConfig  = "0.17.9"
    val Circe       = "0.14.15"
    val Guice       = "7.0.0"
    val Fs2Rabbit   = "5.5.0"
    val Avro        = "1.12.1"
    val CloudEvents = "4.0.1"
    val Log4cats    = "2.7.1"
    val Logback     = "1.5.23"
  }

  val catsEffect = "org.typelevel" %% "cats-effect" % Versions.CatsEffect
  val ip4s       = "com.comcast"   %% "ip4s-core"   % Versions.Ip4s

  val skunk      = "org.tpolecat" %% "skunk-core" % Versions.Skunk
  val flyway     = "org.flywaydb" % "flyway-database-postgresql" % Versions.Flyway
  val postgresql = "org.postgresql" % "postgresql" % Versions.Postgresql

  val pureConfig = "com.github.pureconfig" %% "pureconfig-core" % Versions.PureConfig
  val pureConfigIp4s = "com.github.pureconfig" %% "pureconfig-ip4s" % Versions.PureConfig

  val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % Versions.Http4s
  val http4sEmberClient = "org.http4s" %% "http4s-ember-client" % Versions.Http4s
  val http4sDsl         = "org.http4s" %% "http4s-dsl"          % Versions.Http4s

  val tapirHttp4sServer = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % Versions.Tapir
  val tapirJsonCirce    = "com.softwaremill.sttp.tapir" %% "tapir-json-circe"    % Versions.Tapir

  val munit           = "org.scalameta" %% "munit"             % Versions.Munit           % Test
  val munitCatsEffect = "org.typelevel" %% "munit-cats-effect" % Versions.MunitCatsEffect % Test

  val grpcNettyShaded = "io.grpc" % "grpc-netty-shaded" % Versions.Grpc

  val circeCore    = "io.circe" %% "circe-core"    % Versions.Circe
  val circeGeneric = "io.circe" %% "circe-generic" % Versions.Circe
  val circeParser  = "io.circe" %% "circe-parser"  % Versions.Circe
  val guice        = "com.google.inject" % "guice" % Versions.Guice

  val fs2Rabbit = "dev.profunktor" %% "fs2-rabbit" % Versions.Fs2Rabbit
  val avro      = "org.apache.avro" % "avro"       % Versions.Avro
  val cloudEventsCore = "io.cloudevents" % "cloudevents-core" % Versions.CloudEvents
  val cloudEventsJson = "io.cloudevents" % "cloudevents-json-jackson" % Versions.CloudEvents

  val log4catsCore  = "org.typelevel" %% "log4cats-core"  % Versions.Log4cats
  val log4catsSlf4j = "org.typelevel" %% "log4cats-slf4j" % Versions.Log4cats
  val logback       = "ch.qos.logback" % "logback-classic" % Versions.Logback

  val sharedKernelDependencies: Seq[ModuleID] = Seq(
    catsEffect,
    ip4s,
    circeCore,
    circeGeneric,
    circeParser,
    cloudEventsCore,
    cloudEventsJson,
    log4catsCore,
    munit,
    munitCatsEffect
  )

  val apiGatewayDependencies: Seq[ModuleID] = Seq(
    http4sEmberServer,
    http4sEmberClient,
    http4sDsl,
    tapirHttp4sServer,
    tapirJsonCirce,
    pureConfig,
    pureConfigIp4s,
    log4catsCore,
    munit,
    munitCatsEffect
  )

  val tenantServiceDependencies: Seq[ModuleID] = Seq(
    catsEffect,
    grpcNettyShaded,
    skunk,
    flyway,
    postgresql,
    pureConfig,
    pureConfigIp4s,
    circeCore,
    circeGeneric,
    circeParser,
    guice,
    fs2Rabbit,
    avro,
    cloudEventsCore,
    cloudEventsJson,
    log4catsCore,
    log4catsSlf4j,
    logback,
    munit,
    munitCatsEffect
  )
}
