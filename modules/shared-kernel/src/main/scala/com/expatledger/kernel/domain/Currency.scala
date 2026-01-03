package com.expatledger.kernel.domain

import io.circe.{Decoder, Encoder}

opaque type Currency <: String = String
object Currency:
  def apply(value: String): Currency = value.toUpperCase
  def unsafe(value: String): Currency = value
  given Encoder[Currency] = Encoder.encodeString.contramap(identity)
  given Decoder[Currency] = Decoder.decodeString.map(apply)
