package com.expatledger.kernel.domain

trait DomainEvent extends Event:
  def eventType: String
  def toOutboxEvent: OutboxEvent
