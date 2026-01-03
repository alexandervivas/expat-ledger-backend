package com.expatledger.tenants.infrastructure.messaging

import com.expatledger.kernel.application.EventPublisher
import com.expatledger.kernel.infrastructure.messaging.CloudEventHeaderBuilder
import cats.effect.Sync
import dev.profunktor.fs2rabbit.model.*
import cats.syntax.all.*
import com.expatledger.kernel.domain.events.OutboxEvent
import io.circe.parser.decode
import com.expatledger.tenants.domain.events.TenantCreated
import com.expatledger.kernel.infrastructure.messaging.AvroSerializer

class RabbitMQPublisher[F[_]: Sync](
    publisher: AmqpMessage[Array[Byte]] => F[Unit]
) extends EventPublisher[F]:

  override def publish(event: OutboxEvent): F[Unit] =
    for {
      avroBytes <- Sync[F].delay(serializePayload(event))
      headers = CloudEventHeaderBuilder.buildHeaders(event, "tenants-service")
      props = AmqpProperties.empty.copy(
        headers = headers,
        contentEncoding = Some("UTF-8"),
        contentType = Some("application/avro")
      )
      _ <- publisher(AmqpMessage(avroBytes, props))
    } yield ()

  private def serializePayload(event: OutboxEvent): Array[Byte] =
    event.eventType match
      case "TenantCreated" =>
        decode[TenantCreated](event.payload) match
          case Right(tc) => AvroSerializer.serialize(tc.toAvroRecord, tc.avroSchema)
          case Left(err) => throw new RuntimeException(s"Failed to decode TenantCreated: $err")
      case _ => throw new RuntimeException(s"Unknown event type: ${event.eventType}")
