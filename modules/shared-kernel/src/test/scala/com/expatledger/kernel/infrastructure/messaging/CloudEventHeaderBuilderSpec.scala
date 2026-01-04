package com.expatledger.kernel.infrastructure.messaging

import com.expatledger.kernel.domain.events.OutboxEvent
import munit.FunSuite
import java.time.OffsetDateTime
import java.util.UUID
import io.circe.Json

class CloudEventHeaderBuilderSpec extends FunSuite:

  test("buildHeaders should build correct CloudEvent headers") {
    val event = OutboxEvent(
      id = UUID.randomUUID(),
      aggregateType = "TestAggregate",
      aggregateId = UUID.randomUUID(),
      eventType = "TestEvent",
      payload = Json.obj("test" -> Json.fromString("test")),
      avroPayload = Array.emptyByteArray,
      schemaUrn = "urn:test:schema",
      occurredAt = OffsetDateTime.parse("2023-10-27T10:15:30Z")
    )
    val source = "test-source"

    val headers = CloudEventHeaderBuilder.buildHeaders(event, source)

    assertEquals(headers("ce_specversion"), "1.0")
    assertEquals(headers("ce_id"), event.id.toString)
    assertEquals(headers("ce_source"), source)
    assertEquals(headers("ce_type"), event.eventType)
    assertEquals(headers("ce_time"), "2023-10-27T10:15:30Z")
    assertEquals(headers("ce_datacontenttype"), "application/avro")
    assertEquals(headers("ce_dataschema"), event.schemaUrn)
  }
