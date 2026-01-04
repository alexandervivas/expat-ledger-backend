package com.expatledger.tenants.infrastructure.messaging

import cats.effect.Sync
import com.expatledger.kernel.application.EventPublisher
import com.expatledger.kernel.domain.events.OutboxEvent
import com.expatledger.kernel.infrastructure.messaging.CloudEventHeaderBuilder
import dev.profunktor.fs2rabbit.model.*

class RabbitMQPublisher[F[_]: Sync](
    publisher: AmqpMessage[Array[Byte]] => F[Unit]
) extends EventPublisher[F]:

  override def publish(event: OutboxEvent): F[Unit] = {
    val headers = CloudEventHeaderBuilder.buildHeaders(event, "tenants-service")
    val props = AmqpProperties.empty.copy(
      headers = headers,
      contentEncoding = Some("UTF-8"),
      contentType = Some("application/avro")
    )
    publisher(AmqpMessage(event.avroPayload, props))
  }
