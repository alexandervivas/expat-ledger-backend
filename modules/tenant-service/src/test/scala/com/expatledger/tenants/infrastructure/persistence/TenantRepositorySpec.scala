package com.expatledger.tenants.infrastructure.persistence

import cats.effect.*
import com.expatledger.kernel.domain.model.Currency
import com.expatledger.tenants.domain.model.*

import java.time.{OffsetDateTime, ZoneOffset}
import java.util.UUID

class TenantRepositorySpec extends SkunkPostgresSpec {

  test("TenantRepository should save and find a tenant") {
    withContainers { container =>
      sessionPool(container).use { pool =>
        val repo = TenantRepositoryLive.make[IO](pool)
        val tenantId = TenantId(UUID.randomUUID())
        val now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS)
        val tenant = Tenant(
          id = tenantId,
          name = "Test Tenant",
          reportingCurrency = Currency("USD"),
          taxResidencies = Set(TaxResidency("US"), TaxResidency("CO")),
          createdAt = now,
          updatedAt = now
        )

        for {
          _ <- repo.save(tenant)
          found <- repo.findById(tenantId)
        } yield {
          assertEquals(found, Some(tenant))
        }
      }
    }
  }

  test("TenantRepository should return None for non-existent tenant") {
    withContainers { container =>
      sessionPool(container).use { pool =>
        val repo = TenantRepositoryLive.make[IO](pool)
        val tenantId = TenantId(UUID.randomUUID())

        for {
          found <- repo.findById(tenantId)
        } yield {
          assertEquals(found, None)
        }
      }
    }
  }
}
