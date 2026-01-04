package com.expatledger.tenants.infrastructure.messaging

import java.util.UUID
import java.time.OffsetDateTime
import cats.effect.*
import com.expatledger.kernel.domain.events.OutboxEvent
import munit.CatsEffectSuite
import dev.profunktor.fs2rabbit.model.AmqpMessage

class RabbitMQPublisherSpec extends CatsEffectSuite:

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
      assertEquals(capturedMessage.get.payload.toList, expectedBytes.toList)
    }
  }

  test("RabbitMQPublisher should fail if avroPayload is missing (fallback not implemented)") {
    val publisherFunc: AmqpMessage[Array[Byte]] => IO[Unit] = _ => IO.unit
    val rabbitPublisher = new RabbitMQPublisher[IO](publisherFunc)

    val event = OutboxEvent(
      id = UUID.randomUUID(),
      aggregateType = "Tenant",
      aggregateId = UUID.randomUUID(),
      eventType = "TenantCreated",
      payload = "{}",
      avroPayload = Array.emptyByteArray,
      schemaUrn = "urn:test",
      occurredAt = OffsetDateTime.now()
    )

    interceptIO[IllegalStateException] {
      rabbitPublisher.publish(event)
    }.map { ex =>
      assert(ex.getMessage.contains("fallback serialization is not implemented"))
    }
  }
