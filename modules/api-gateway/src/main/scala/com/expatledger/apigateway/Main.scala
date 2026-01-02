package com.expatledger.apigateway

import cats.effect.{IO, IOApp, ExitCode}
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import com.expatledger.apigateway.routes.HealthRoutes
import com.expatledger.apigateway.config.*
import com.expatledger.apigateway.discovery.StaticServiceDiscovery

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      tenantServiceConfig <- StaticServiceDiscovery.getServiceConfig("tenant-service")
      
      config = ApiGatewayConfig(
        host = host"0.0.0.0",
        port = port"8080",
        services = ServiceConfig(
          tenantService = tenantServiceConfig
        )
      )

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
