package com.expatledger.kernel.infrastructure.messaging

import com.expatledger.kernel.domain.events.OutboxEvent
import dev.profunktor.fs2rabbit.model.*

import java.time.ZoneOffset

object CloudEventHeaderBuilder:
  def buildHeaders(event: OutboxEvent, source: String): Headers =
    Headers(
      "ce_specversion" := "1.0",
      "ce_id" := event.id.toString,
      "ce_source" := source,
      "ce_type" := event.eventType.entryName,
      "ce_time" := event.occurredAt.atZoneSameInstant(ZoneOffset.UTC).toString,
      "ce_datacontenttype" := "application/avro",
      "ce_dataschema" := event.schemaUrn
    )
