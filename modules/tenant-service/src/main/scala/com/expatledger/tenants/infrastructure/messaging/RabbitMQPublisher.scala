package com.expatledger.tenants.infrastructure.messaging

import com.expatledger.kernel.application.EventPublisher
import com.expatledger.kernel.domain.OutboxEvent
import cats.effect.Sync
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder

import java.net.URI
import java.time.ZoneOffset
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.*
import dev.profunktor.fs2rabbit.effects.MessageEncoder
import cats.data.Kleisli
import cats.syntax.all.*
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter

import java.io.ByteArrayOutputStream

class RabbitMQPublisher[F[_]: Sync](
    rabbit: RabbitClient[F],
    exchangeName: ExchangeName,
    routingKey: RoutingKey
)(using channel: AMQPChannel) extends EventPublisher[F]:

  implicit val byteArrayEncoder: MessageEncoder[F, Array[Byte]] =
    Kleisli(payload => Sync[F].pure(AmqpMessage(payload, AmqpProperties.empty)))

  private val avroSchemaJson = """
    {
      "type": "record",
      "name": "CloudEvent",
      "namespace": "com.expatledger.events",
      "fields": [
        {"name": "id", "type": "string"},
        {"name": "source", "type": "string"},
        {"name": "type", "type": "string"},
        {"name": "specversion", "type": "string"},
        {"name": "datacontenttype", "type": ["null", "string"], "default": null},
        {"name": "dataschema", "type": ["null", "string"], "default": null},
        {"name": "subject", "type": ["null", "string"], "default": null},
        {"name": "time", "type": ["null", "string"], "default": null},
        {"name": "data", "type": ["null", "bytes"], "default": null}
      ]
    }
  """
  private val schema = new Schema.Parser().parse(avroSchemaJson)

  override def publish(event: OutboxEvent): F[Unit] =
    for {
      ce <- Sync[F].delay(toCloudEvent(event))
      avroBytes <- Sync[F].delay(serializeAvro(ce))
      _ <- rabbit.createPublisher[Array[Byte]](exchangeName, routingKey).flatMap { pub =>
        pub(avroBytes)
      }
    } yield ()

  private def toCloudEvent(event: OutboxEvent): CloudEvent =
    CloudEventBuilder.v1()
      .withId(event.id.toString)
      .withSource(URI.create(s"/tenants/${event.aggregateId}"))
      .withType(event.eventType)
      .withTime(event.occurredAt.atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime)
      .withData("application/json", event.payload.getBytes)
      .build()

  private def serializeAvro(ce: CloudEvent): Array[Byte] =
    val record = new GenericData.Record(schema)
    record.put("id", ce.getId)
    record.put("source", ce.getSource.toString)
    record.put("type", ce.getType)
    record.put("specversion", ce.getSpecVersion.toString)
    record.put("datacontenttype", ce.getDataContentType)
    record.put("dataschema", Option(ce.getDataSchema).map(_.toString).orNull)
    record.put("subject", ce.getSubject)
    record.put("time", Option(ce.getTime).map(_.toString).orNull)
    record.put("data", java.nio.ByteBuffer.wrap(ce.getData.toBytes))

    val out = new ByteArrayOutputStream()
    val writer = new SpecificDatumWriter[GenericRecord](schema)
    val encoder = EncoderFactory.get().binaryEncoder(out, null)
    writer.write(record, encoder)
    encoder.flush()
    out.toByteArray
