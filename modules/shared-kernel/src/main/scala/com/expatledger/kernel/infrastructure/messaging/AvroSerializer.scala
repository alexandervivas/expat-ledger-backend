package com.expatledger.kernel.infrastructure.messaging

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import java.io.ByteArrayOutputStream

object AvroSerializer:
  def serialize(record: GenericRecord, schema: Schema): Array[Byte] =
    val out = new ByteArrayOutputStream()
    try {
      val writer = new SpecificDatumWriter[GenericRecord](schema)
      val encoder = EncoderFactory.get().binaryEncoder(out, null)
      writer.write(record, encoder)
      encoder.flush()
      out.toByteArray
    } finally {
      out.close()
    }
