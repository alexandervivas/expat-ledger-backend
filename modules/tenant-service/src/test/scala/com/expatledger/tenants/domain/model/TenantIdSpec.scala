package com.expatledger.tenants.domain.model

import munit.FunSuite
import java.util.UUID
import io.circe.syntax.*
import io.circe.parser.decode

class TenantIdSpec extends FunSuite:

  test("TenantId codec should serialize and deserialize correctly") {
    val uuid = UUID.randomUUID()
    val tenantId = TenantId(uuid)

    val json = tenantId.asJson.noSpaces
    assertEquals(json, s"\"${uuid.toString}\"")

    val decoded = decode[TenantId](json)
    assertEquals(decoded, Right(tenantId))
  }

  test("TenantId.generate should create a valid TenantId") {
    val tenantId = TenantId.generate
    assert(tenantId.isInstanceOf[UUID])
  }
