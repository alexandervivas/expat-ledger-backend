package com.expatledger.tenants.domain.events

import munit.FunSuite
import com.expatledger.tenants.domain.model.{Tenant, TenantId, TaxResidency}
import com.expatledger.kernel.domain.model.Currency
import java.time.OffsetDateTime
import java.util.UUID

class TenantCreatedSpec extends FunSuite:

  test("TenantCreated should load schema and generate Avro record") {
    val tenantId = TenantId.generate
    val tenant = Tenant(
      id = tenantId,
      name = "Test Tenant",
      reportingCurrency = Currency("USD"),
      taxResidencies = Set(TaxResidency("US")),
      createdAt = OffsetDateTime.now(),
      updatedAt = OffsetDateTime.now()
    )
    val event = TenantCreated(tenant, OffsetDateTime.now())

    val schema = event.avroSchema
    assert(schema != null)
    assertEquals(schema.getName, "TenantCreated")
    assertEquals(schema.getNamespace, "com.expatledger.events.v1")

    val record = event.toAvroRecord
    assertEquals(record.get("name").toString, "Test Tenant")
    assertEquals(record.get("tenantId").toString, (tenantId: UUID).toString)
    assert(record.get("eventId") != null)
  }
