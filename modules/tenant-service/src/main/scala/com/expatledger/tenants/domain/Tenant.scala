package com.expatledger.tenants.domain

import java.time.OffsetDateTime
import com.expatledger.kernel.domain.Currency
import io.circe.Codec as CirceCodec
import io.circe.generic.semiauto.*

case class Tenant(
    id: TenantId,
    name: String,
    reportingCurrency: Currency,
    taxResidencies: Set[TaxResidency],
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
object Tenant:
  import com.expatledger.kernel.domain.Currency.given
  given CirceCodec[Tenant] = deriveCodec
