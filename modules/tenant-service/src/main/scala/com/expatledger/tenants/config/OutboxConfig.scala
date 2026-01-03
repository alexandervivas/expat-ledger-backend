package com.expatledger.tenants.config

import pureconfig.ConfigReader
import scala.concurrent.duration.FiniteDuration

case class OutboxConfig(
    pollInterval: FiniteDuration,
    batchSize: Int
) derives ConfigReader
