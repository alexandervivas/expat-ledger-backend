package com.expatledger.apigateway.config

import com.comcast.ip4s.{Host, Port}

case class ApiGatewayConfig(
    host: Host,
    port: Port,
    services: ServiceConfig
)

case class ServiceConfig(
    tenantService: GrpcServiceConfig
)

case class GrpcServiceConfig(
    host: Host,
    port: Port
)
