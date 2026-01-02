import sbt.*

object Dependencies {
  object Versions {
    val CatsEffect = "3.6.3"
    val Http4s     = "0.23.33"
    val Tapir      = "1.13.4"
    val Munit      = "1.2.1"
    val MunitCatsEffect = "2.1.0"
    val Ip4s        = "3.6.0"
    val Grpc        = "1.78.0"
  }

  val catsEffect = "org.typelevel" %% "cats-effect" % Versions.CatsEffect
  val ip4s       = "com.comcast"   %% "ip4s-core"   % Versions.Ip4s
  
  val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % Versions.Http4s
  val http4sEmberClient = "org.http4s" %% "http4s-ember-client" % Versions.Http4s
  val http4sDsl         = "org.http4s" %% "http4s-dsl"          % Versions.Http4s

  val tapirHttp4sServer = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % Versions.Tapir
  val tapirJsonCirce    = "com.softwaremill.sttp.tapir" %% "tapir-json-circe"    % Versions.Tapir

  val munit           = "org.scalameta" %% "munit"             % Versions.Munit           % Test
  val munitCatsEffect = "org.typelevel" %% "munit-cats-effect" % Versions.MunitCatsEffect % Test
  
  val grpcNettyShaded = "io.grpc" % "grpc-netty-shaded" % Versions.Grpc

  val sharedKernelDependencies: Seq[ModuleID] = Seq(
    catsEffect,
    ip4s,
    munit,
    munitCatsEffect
  )

  val apiGatewayDependencies: Seq[ModuleID] = Seq(
    http4sEmberServer,
    http4sEmberClient,
    http4sDsl,
    tapirHttp4sServer,
    tapirJsonCirce,
    munit,
    munitCatsEffect
  )

  val tenantServiceDependencies: Seq[ModuleID] = Seq(
    catsEffect,
    grpcNettyShaded,
    munit,
    munitCatsEffect
  )
}
