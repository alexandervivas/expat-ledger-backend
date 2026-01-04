package com.expatledger.api.discovery

import com.comcast.ip4s.*
import com.expatledger.api.config.GrpcServiceConfig
import munit.CatsEffectSuite

class StaticServiceDiscoverySpec extends CatsEffectSuite {

  test("getServiceConfig should return correct config for known services") {
    StaticServiceDiscovery.getServiceConfig("tenant-service").map { config =>
      assertEquals(config, GrpcServiceConfig(host"localhost", port"9000"))
    }
  }

  test("getServiceConfig should return error for unknown services") {
    StaticServiceDiscovery.getServiceConfig("unknown-service").intercept[RuntimeException]
  }
}
