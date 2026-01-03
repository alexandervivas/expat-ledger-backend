package com.expatledger.tenants.domain

import java.util.UUID
import java.time.OffsetDateTime
import com.expatledger.kernel.domain.DomainEvent
import io.circe.Codec as CirceCodec
import io.circe.generic.semiauto.*

case class TenantCreated(
    override val id: UUID,
    override val aggregateId: UUID,
    name: String,
    reportingCurrency: String,
    taxResidencies: List[String],
    override val occurredAt: OffsetDateTime
) extends DomainEvent:
  override def eventType: String = "TenantCreated"

object TenantCreated:
  given CirceCodec[TenantCreated] = deriveCodec
