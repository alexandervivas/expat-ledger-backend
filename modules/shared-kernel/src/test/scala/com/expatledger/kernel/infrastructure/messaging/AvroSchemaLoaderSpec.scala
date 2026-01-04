package com.expatledger.kernel.infrastructure.messaging

import munit.FunSuite
import java.io.FileNotFoundException

class AvroSchemaLoaderSpec extends FunSuite {

  test("load should throw FileNotFoundException when schema file is not found") {
    val schemaName = "non-existent.avsc"
    intercept[FileNotFoundException] {
      AvroSchemaLoader.load(schemaName)
    }
  }

  test("load should load an existing schema file from classpath") {
    val schemaName = "tenant-created.avsc"
    val schema = AvroSchemaLoader.load(schemaName)
    assertEquals(schema.getName, "TenantCreated")
    assertEquals(schema.getNamespace, "com.expatledger.events.v1")
  }
}
