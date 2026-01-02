package com.expatledger.api.discovery

import com.expatledger.api.config.GrpcServiceConfig
import cats.effect.IO
import com.comcast.ip4s.{Host, Port}

trait ServiceDiscovery {
  def getServiceConfig(serviceName: String): IO[GrpcServiceConfig]
}

object StaticServiceDiscovery extends ServiceDiscovery {
  private def getEnvServiceConfig(hostEnv: String, portEnv: String, defaultHost: String, defaultPort: Int): IO[GrpcServiceConfig] = {
    for {
      host <- IO.fromOption(Host.fromString(sys.env.getOrElse(hostEnv, defaultHost)))(
        new RuntimeException(s"Invalid host in $hostEnv")
      )
      port <- IO.fromOption(Port.fromString(sys.env.getOrElse(portEnv, defaultPort.toString)))(
        new RuntimeException(s"Invalid port in $portEnv")
      )
    } yield GrpcServiceConfig(host, port)
  }

  override def getServiceConfig(serviceName: String): IO[GrpcServiceConfig] =
    serviceName match {
      case "tenant-service" =>
        getEnvServiceConfig("TENANT_SERVICE_HOST", "TENANT_SERVICE_PORT", "localhost", 9000)
      case _ =>
        IO.raiseError(new RuntimeException(s"Service $serviceName not found in static discovery"))
    }
}
