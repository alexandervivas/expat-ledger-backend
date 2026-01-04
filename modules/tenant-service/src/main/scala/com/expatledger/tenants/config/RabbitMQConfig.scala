package com.expatledger.tenants.config

import pureconfig.ConfigReader

case class RabbitMQConfig(
    host: String,
    port: Int,
    virtualHost: String,
    user: String,
    password: Option[String],
    ssl: Boolean,
    exchange: String,
    routingKey: String
) derives ConfigReader
