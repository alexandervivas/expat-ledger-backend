package com.expatledger.tenants.domain.events

import com.expatledger.kernel.domain.{DomainEvent, OutboxEvent}
import com.expatledger.tenants.domain.model.Tenant
import io.circe.Codec as CirceCodec
import io.circe.generic.semiauto.*
import io.circe.syntax.*

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
  override def eventType: String = "TenantCreated"

  override def toOutboxEvent: OutboxEvent =
    OutboxEvent(
      id = id,
      aggregateType = "Tenant",
      aggregateId = aggregateId,
      eventType = eventType,
      payload = this.asJson.noSpaces,
      occurredAt = occurredAt
    )

object TenantCreated:
  given CirceCodec[TenantCreated] = deriveCodec
  
  def apply(tenant: Tenant, taxResidencies: List[String], occurredAt: OffsetDateTime): TenantCreated =
    TenantCreated(
      id = UUID.randomUUID(),
      aggregateId = tenant.id,
      name = tenant.name,
      reportingCurrency = tenant.reportingCurrency,
      taxResidencies = taxResidencies,
      occurredAt = occurredAt
    )
