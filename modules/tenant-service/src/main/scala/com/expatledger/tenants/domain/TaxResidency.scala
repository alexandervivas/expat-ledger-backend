package com.expatledger.tenants.domain

import io.circe.Codec as CirceCodec
import io.circe.generic.semiauto.*

case class TaxResidency(countryCode: String)
object TaxResidency:
  given CirceCodec[TaxResidency] = deriveCodec
