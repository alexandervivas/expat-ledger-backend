package com.expatledger.kernel.domain.events

trait DomainEvent extends Event with AvroSerializable:
  def eventType: EventType
  def schemaUrn: String
  def toOutboxEvent: OutboxEvent
  def aggregateType: String
