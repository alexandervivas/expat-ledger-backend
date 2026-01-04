package com.expatledger.tenants.infrastructure.messaging

import cats.effect.Sync
import com.expatledger.kernel.application.EventPublisher
import com.expatledger.kernel.domain.events.OutboxEvent
import com.expatledger.kernel.infrastructure.messaging.CloudEventHeaderBuilder
import dev.profunktor.fs2rabbit.model.*
import dev.profunktor.fs2rabbit.model.AmqpFieldValue.StringVal

class RabbitMQPublisher[F[_]: Sync](
    publisher: AmqpMessage[Array[Byte]] => F[Unit]
) extends EventPublisher[F]:

  override def publish(event: OutboxEvent): F[Unit] = {
    if (event.avroPayload.isEmpty) {
      Sync[F].raiseError[Unit](
        new IllegalStateException(s"Event ${event.id} has an empty avroPayload and fallback serialization is not implemented.")
      )
    } else {
      val rawHeaders = CloudEventHeaderBuilder.buildHeaders(event, "tenants-service")
      val amqpHeaders = Headers(rawHeaders.map { (k, v) =>
        k -> (StringVal(v): AmqpFieldValue)
      })
      val props = AmqpProperties.empty.copy(
        headers = amqpHeaders,
        contentEncoding = Some("UTF-8"),
        contentType = Some("application/avro")
      )
      publisher(AmqpMessage(event.avroPayload, props))
    }
  }
