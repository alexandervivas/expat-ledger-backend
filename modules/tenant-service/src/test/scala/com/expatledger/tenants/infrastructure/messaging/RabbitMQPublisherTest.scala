package com.expatledger.tenants.infrastructure.messaging

import java.util.UUID
import java.time.OffsetDateTime
import cats.effect.*
import com.expatledger.kernel.domain.events.OutboxEvent
import munit.CatsEffectSuite
import dev.profunktor.fs2rabbit.model.AmqpMessage

class RabbitMQPublisherTest extends CatsEffectSuite:

  test("RabbitMQPublisher should use avroPayload if present") {
    var capturedMessage: Option[AmqpMessage[Array[Byte]]] = None
    val publisherFunc: AmqpMessage[Array[Byte]] => IO[Unit] = msg => IO { capturedMessage = Some(msg) }
    val rabbitPublisher = new RabbitMQPublisher[IO](publisherFunc)

    val expectedBytes = "avro-bytes".getBytes
    val event = OutboxEvent(
      id = UUID.randomUUID(),
      aggregateType = "Tenant",
      aggregateId = UUID.randomUUID(),
      eventType = "TenantCreated",
      payload = "{}",
      avroPayload = expectedBytes,
      schemaUrn = "urn:test",
      occurredAt = OffsetDateTime.now()
    )

    rabbitPublisher.publish(event).map { _ =>
      assert(capturedMessage.isDefined)
      assertEquals(capturedMessage.get.payload, expectedBytes)
    }
  }

  test("RabbitMQPublisher should fallback to polymorphic serialization if avroPayload is missing") {
    var capturedMessage: Option[AmqpMessage[Array[Byte]]] = None
    val publisherFunc: AmqpMessage[Array[Byte]] => IO[Unit] = msg => IO { capturedMessage = Some(msg) }
    val rabbitPublisher = new RabbitMQPublisher[IO](publisherFunc)

    val tenantId = UUID.randomUUID()
    val eventId = UUID.randomUUID()
    val occurredAt = OffsetDateTime.now()

    // We need a valid JSON for TenantCreated because the serializer will decode it
    val payload = s"""{
      "id": "$eventId",
      "aggregateId": "$tenantId",
      "name": "Test Tenant",
      "reportingCurrency": "USD",
      "taxResidencies": [],
      "occurredAt": "${occurredAt.toString}"
    }"""

    val event = OutboxEvent(
      id = eventId,
      aggregateType = "Tenant",
      aggregateId = tenantId,
      eventType = "TenantCreated",
      payload = payload,
      avroPayload = Array.emptyByteArray,
      schemaUrn = "urn:test",
      occurredAt = occurredAt
    )

    rabbitPublisher.publish(event).map { _ =>
      assert(capturedMessage.isDefined)
      assert(capturedMessage.get.payload.length > 0)
    }
  }
