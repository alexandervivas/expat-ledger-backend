package com.expatledger.tenants.domain

import java.util.UUID
import io.circe.{Codec as CirceCodec, Decoder as CirceDecoder, Encoder as CirceEncoder}

opaque type TenantId <: UUID = UUID
object TenantId:
  def apply(uuid: UUID): TenantId = uuid
  def generate: TenantId = UUID.randomUUID()
  given CirceCodec[TenantId] = CirceCodec.from(
    CirceDecoder.decodeUUID.map(apply),
    CirceEncoder.encodeUUID.contramap(identity)
  )
