package com.expatledger.kernel.domain.events

trait EventSerializer:
  def serialize(payload: String): Array[Byte]
