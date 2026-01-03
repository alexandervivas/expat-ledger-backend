package com.expatledger.tenants.domain.model

import com.expatledger.kernel.domain.model.Currency
import io.circe.Codec as CirceCodec
import io.circe.generic.semiauto.*

import java.time.OffsetDateTime

case class Tenant(
    id: TenantId,
    name: String,
    reportingCurrency: Currency,
    taxResidencies: Set[TaxResidency],
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
object Tenant:
  import Currency.given
  given CirceCodec[Tenant] = deriveCodec
