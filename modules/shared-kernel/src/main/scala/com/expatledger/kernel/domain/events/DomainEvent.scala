package com.expatledger.kernel.domain.events

trait DomainEvent extends Event with AvroSerializable:
  def eventType: String
  def schemaUrn: String
  def toOutboxEvent: OutboxEvent
  def aggregateType: String
