package com.expatledger.kernel.infrastructure.messaging

import org.apache.avro.Schema

import java.io.FileNotFoundException
import scala.io.Source
import scala.util.Using

object AvroSchemaLoader:
  def load(schemaName: String): Schema =
    val path = s"events/v1/$schemaName"
    val resource = getClass.getClassLoader.getResourceAsStream(path)
    if resource == null then
      throw new FileNotFoundException(s"Schema file not found in classpath: $path")

    val schemaJson = Using.resource(resource) { is =>
      Source.fromInputStream(is).mkString
    }
    new Schema.Parser().parse(schemaJson)
