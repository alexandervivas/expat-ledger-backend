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
}
