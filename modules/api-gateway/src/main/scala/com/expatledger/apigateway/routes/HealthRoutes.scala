package com.expatledger.apigateway.routes

import cats.effect.IO
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.HttpRoutes

object HealthRoutes {
  val healthEndpoint = endpoint.get
    .in("health")
    .out(stringBody)
    .description("Health check endpoint")

  val healthServerEndpoint = healthEndpoint.serverLogicSuccess(_ => IO.pure("OK"))

  val routes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(healthServerEndpoint)
}
