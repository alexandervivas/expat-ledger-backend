package com.expatledger.api.routes

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.{Method, Request, Status, Uri}

class HealthRoutesSpec extends CatsEffectSuite {

  test("Health check endpoint returns 200 OK") {
    val request = Request[IO](Method.GET, Uri.unsafeFromString("/health"))
    HealthRoutes.routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map(assertEquals(_, "OK"))
    }
  }
}
