package com.expatledger.tenants.service

import java.util.UUID
import cats.effect.*
import com.expatledger.tenants.domain.*
import com.expatledger.tenants.persistence.{TenantRepository, OutboxRepository, Repository}
import com.expatledger.kernel.domain.OutboxEvent
import munit.CatsEffectSuite
import skunk.Session

class TenantServiceTest extends CatsEffectSuite:

  class MockTenantRepository extends TenantRepository[IO]:
    var savedTenant: Option[Tenant] = None
    override def save(tenant: Tenant): IO[Unit] = IO { savedTenant = Some(tenant) }
    override def findById(id: TenantId): IO[Option[Tenant]] = IO.none

  class MockOutboxRepository extends OutboxRepository[IO]:
    var savedEvent: Option[OutboxEvent] = None
    override def save(event: OutboxEvent): IO[Unit] = IO { savedEvent = Some(event) }
    override def saveAll(events: List[OutboxEvent]): IO[Unit] = IO.unit

  class MockBaseRepository extends Repository[IO]:
    override def session: Session[IO] = null // Not used in atomic mock
    override def atomic[A](action: Session[IO] => IO[A])(using F: MonadCancelThrow[IO]): IO[A] =
      action(null)

  test("onboardTenant should save tenant and outbox event") {
    val tenantRepo = new MockTenantRepository
    val outboxRepo = new MockOutboxRepository
    val baseRepo = new MockBaseRepository
    val service = new TenantServiceLive[IO](tenantRepo, outboxRepo, baseRepo)

    val request = OnboardTenantRequest("Test Tenant", "USD", "US")

    service.onboardTenant(request).map { tenantId =>
      assert(tenantRepo.savedTenant.isDefined)
      assertEquals(tenantRepo.savedTenant.get.name, "Test Tenant")
      assertEquals(tenantRepo.savedTenant.get.reportingCurrency, "USD")
      assertEquals(tenantRepo.savedTenant.get.taxResidencies.head.countryCode, "US")

      assert(outboxRepo.savedEvent.isDefined)
      val event = outboxRepo.savedEvent.get
      assertEquals(event.aggregateType, "Tenant")
      assertEquals(event.aggregateId, tenantId: UUID)
      assertEquals(event.eventType, "TenantCreated")
      assert(event.payload.contains("Test Tenant"))
    }
  }
