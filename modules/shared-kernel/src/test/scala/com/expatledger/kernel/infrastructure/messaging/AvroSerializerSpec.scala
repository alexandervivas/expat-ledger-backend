package com.expatledger.kernel.infrastructure.messaging

import munit.FunSuite
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData

class AvroSerializerSpec extends FunSuite:

  test("AvroSerializer should serialize a GenericRecord") {
    val schemaJson =
      """
        |{
        |  "type": "record",
        |  "name": "TestRecord",
        |  "fields": [
        |    {"name": "field1", "type": "string"},
        |    {"name": "field2", "type": "int"}
        |  ]
        |}
        |""".stripMargin
    val schema = new Schema.Parser().parse(schemaJson)
    val record = new GenericData.Record(schema)
    record.put("field1", "hello")
    record.put("field2", 42)

    val bytes = AvroSerializer.serialize(record, schema)
    assert(bytes.nonEmpty)
  }
