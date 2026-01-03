package com.expatledger.tenants.config

import pureconfig.ConfigReader

case class DatabaseConfig(
    host: String,
    port: Int,
    name: String,
    user: String,
    password: Option[String]
) derives ConfigReader
