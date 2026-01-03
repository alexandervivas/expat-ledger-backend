package com.expatledger.api.config

import pureconfig.ConfigReader

case class ServiceConfig(
    tenantService: GrpcServiceConfig
) derives ConfigReader
