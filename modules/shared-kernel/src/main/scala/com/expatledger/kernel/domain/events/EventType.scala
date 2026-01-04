package com.expatledger.kernel.domain.events

import enumeratum.{Enum, EnumEntry}
import io.circe.{Decoder, Encoder}

sealed trait EventType extends EnumEntry

object EventType extends Enum[EventType] {
  val values: IndexedSeq[EventType] = findValues

  case object TenantCreated extends EventType

  implicit val encoder: Encoder[EventType] = Encoder.encodeString.contramap(_.entryName)
  implicit val decoder: Decoder[EventType] = Decoder.decodeString.emap { s =>
    withNameOption(s).toRight(s"Unknown EventType: $s")
  }
}
