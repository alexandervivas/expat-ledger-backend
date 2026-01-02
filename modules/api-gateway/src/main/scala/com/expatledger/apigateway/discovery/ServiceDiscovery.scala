package com.expatledger.apigateway.discovery

import com.expatledger.apigateway.config.GrpcServiceConfig
import cats.effect.IO

trait ServiceDiscovery {
  def getServiceConfig(serviceName: String): IO[GrpcServiceConfig]
}

object StaticServiceDiscovery extends ServiceDiscovery {
  private val services = Map(
    "tenant-service" -> GrpcServiceConfig(
      sys.env.getOrElse("TENANT_SERVICE_HOST", "localhost"),
      sys.env.getOrElse("TENANT_SERVICE_PORT", "9000").toInt
    )
  )

  override def getServiceConfig(serviceName: String): IO[GrpcServiceConfig] =
    IO.fromOption(services.get(serviceName))(
      new RuntimeException(s"Service $serviceName not found in static discovery")
    )
}
