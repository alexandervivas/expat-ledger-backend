package com.expatledger.tenants.config

import pureconfig.ConfigReader
import scala.concurrent.duration.FiniteDuration

case class OutboxConfig(
    pollInterval: FiniteDuration,
    batchSize: Int,
    retryInitialDelay: FiniteDuration,
    retryCount: Int
) derives ConfigReader
