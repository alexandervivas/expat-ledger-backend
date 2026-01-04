package com.expatledger.kernel.domain.events

import munit.FunSuite
import io.circe.syntax.*
import io.circe.parser.decode

class EventTypeSpec extends FunSuite {

  test("EventType serialization") {
    val eventType: EventType = EventType.TenantCreated
    val json = eventType.asJson.noSpaces
    assertEquals(json, "\"TenantCreated\"")
  }

  test("EventType deserialization") {
    val json = "\"TenantCreated\""
    val result = decode[EventType](json)
    assertEquals(result, Right(EventType.TenantCreated))
  }

  test("EventType deserialization failure") {
    val json = "\"UnknownEvent\""
    val result = decode[EventType](json)
    assert(result.isLeft)
  }
}
