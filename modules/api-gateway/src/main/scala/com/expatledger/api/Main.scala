package com.expatledger.api

import cats.effect.{IO, IOApp, ExitCode}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import com.expatledger.api.routes.HealthRoutes
import com.expatledger.api.config.*
import pureconfig.ConfigSource

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      config <- IO.blocking(ConfigSource.default.loadOrThrow[ApiGatewayConfig])

      httpApp = Router("/" -> HealthRoutes.routes).orNotFound

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(config.host)
        .withPort(config.port)
        .withHttpApp(httpApp)
        .build
        .use(_ => IO.never)
    } yield ExitCode.Success
  }
}
