package com.expatledger.api.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.module.ip4s.*

case class GrpcServiceConfig(
    host: Host,
    port: Port
) derives ConfigReader
