package com.expatledger.tenants.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.module.ip4s.*

case class TenantServiceConfig(
    host: Host,
    port: Port,
    db: DatabaseConfig,
    rabbitmq: RabbitMQConfig,
    outbox: OutboxConfig
) derives ConfigReader
