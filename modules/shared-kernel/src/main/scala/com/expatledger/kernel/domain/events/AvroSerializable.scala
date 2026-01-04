package com.expatledger.kernel.domain.events

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord

trait AvroSerializable:
  def avroSchema: Schema
  def toAvroRecord: GenericRecord
