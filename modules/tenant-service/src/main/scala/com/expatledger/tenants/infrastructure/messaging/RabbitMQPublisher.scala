package com.expatledger.tenants.infrastructure.messaging

import com.expatledger.kernel.application.EventPublisher
import com.expatledger.kernel.infrastructure.messaging.CloudEventHeaderBuilder
import cats.effect.Sync
import dev.profunktor.fs2rabbit.model.*
import cats.syntax.all.*
import com.expatledger.kernel.domain.events.OutboxEvent

class RabbitMQPublisher[F[_]: Sync](
    publisher: AmqpMessage[Array[Byte]] => F[Unit]
) extends EventPublisher[F]:

  override def publish(event: OutboxEvent): F[Unit] =
    for {
      avroBytes <- Sync[F].delay(event.avroPayload)
      headers = CloudEventHeaderBuilder.buildHeaders(event, "tenants-service")
      props = AmqpProperties.empty.copy(
        headers = headers,
        contentEncoding = Some("UTF-8"),
        contentType = Some("application/avro")
      )
      _ <- publisher(AmqpMessage(avroBytes, props))
    } yield ()
