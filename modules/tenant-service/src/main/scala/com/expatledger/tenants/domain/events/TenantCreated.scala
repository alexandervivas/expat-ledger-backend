package com.expatledger.tenants.domain.events

import com.expatledger.kernel.domain.events.{DomainEvent, EventType, OutboxEvent}
import com.expatledger.kernel.infrastructure.messaging.AvroSchemaLoader
import com.expatledger.tenants.domain.model.Tenant
import io.circe.Codec as CirceCodec
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}

import java.time.OffsetDateTime
import java.util.UUID

case class TenantCreated(
    override val id: UUID,
    override val aggregateId: UUID,
    name: String,
    reportingCurrency: String,
    taxResidencies: List[String],
    override val occurredAt: OffsetDateTime
) extends DomainEvent:
  override def eventType: EventType = EventType.TenantCreated
  override def aggregateType: String = "Tenant"
  override def schemaUrn: String = s"urn:avro:schema:com.expatledger.events.v1.${eventType.entryName}"

  override def toOutboxEvent: OutboxEvent =
    OutboxEvent(
      id = id,
      aggregateType = aggregateType,
      aggregateId = aggregateId,
      eventType = eventType,
      payload = this.asJson.noSpaces,
      occurredAt = occurredAt,
      schemaUrn = schemaUrn
    )

  override def avroSchema: Schema = TenantCreated.schema

  override def toAvroRecord: GenericRecord =
    val record = new GenericData.Record(avroSchema)
    record.put("eventId", id.toString)
    record.put("occurredAt", occurredAt.toString)
    record.put("tenantId", aggregateId.toString)
    record.put("name", name)
    record

object TenantCreated:
  val schema: Schema = AvroSchemaLoader.load("tenant-created.avsc")

  given CirceCodec[TenantCreated] = deriveCodec

  def apply(tenant: Tenant, occurredAt: OffsetDateTime): TenantCreated =
    TenantCreated(
      id = UUID.randomUUID(),
      aggregateId = tenant.id,
      name = tenant.name,
      reportingCurrency = tenant.reportingCurrency,
      taxResidencies = tenant.taxResidencies.toList.map(_.countryCode),
      occurredAt = occurredAt
    )
