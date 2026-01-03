package com.expatledger.api.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.module.ip4s.*

case class ApiGatewayConfig(
    host: Host,
    port: Port,
    services: ServiceConfig
) derives ConfigReader
