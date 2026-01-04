package com.expatledger.tenants.domain.events

import com.expatledger.kernel.domain.events.EventSerializer
import com.expatledger.kernel.infrastructure.messaging.AvroSerializer
import io.circe.parser.decode

class TenantCreatedSerializer extends EventSerializer:
  override def serialize(payload: String): Array[Byte] =
    decode[TenantCreated](payload) match
      case Right(tc) => AvroSerializer.serialize(tc.toAvroRecord, tc.avroSchema)
      case Left(err) => throw new RuntimeException(s"Failed to decode TenantCreated: $err")
