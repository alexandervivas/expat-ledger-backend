package com.expatledger.tenants.domain.model

import munit.FunSuite
import java.time.OffsetDateTime
import com.expatledger.kernel.domain.model.Currency
import io.circe.syntax.*
import io.circe.parser.decode

class TenantSpec extends FunSuite:

  test("Tenant codec should serialize and deserialize correctly") {
    val tenant = Tenant(
      id = TenantId.generate,
      name = "Test Tenant",
      reportingCurrency = Currency("USD"),
      taxResidencies = Set(TaxResidency("US"), TaxResidency("CA")),
      createdAt = OffsetDateTime.parse("2023-10-27T10:15:30Z"),
      updatedAt = OffsetDateTime.parse("2023-10-27T10:15:30Z")
    )

    val json = tenant.asJson.noSpaces
    val decoded = decode[Tenant](json)

    assertEquals(decoded, Right(tenant))
  }
