package com.expatledger.tenants.application

import java.util.UUID
import cats.effect.*
import com.expatledger.kernel.domain.events.OutboxEvent
import com.expatledger.tenants.domain.*
import com.expatledger.kernel.domain.repositories.OutboxRepository
import com.expatledger.tenants.domain.model.{Tenant, TenantId}
import com.expatledger.tenants.domain.repositories.TenantRepository
import munit.CatsEffectSuite

import java.time.OffsetDateTime

class TenantServiceSpec extends CatsEffectSuite:

  class MockTenantRepository extends TenantRepository[IO]:
    var savedTenant: Option[Tenant] = None
    var tenantsById: Map[TenantId, Tenant] = Map.empty
    override def save(tenant: Tenant): IO[Unit] = IO { savedTenant = Some(tenant) }
    override def findById(id: TenantId): IO[Option[Tenant]] = IO.pure(tenantsById.get(id))

  class MockOutboxRepository extends OutboxRepository[IO]:
    var savedEvent: Option[OutboxEvent] = None
    override def save(event: OutboxEvent): IO[Unit] = IO { savedEvent = Some(event) }
    override def fetchUnprocessed(limit: Int): IO[List[OutboxEvent]] = IO.pure(Nil)
    override def markProcessed(ids: List[java.util.UUID]): IO[Unit] = IO.unit

  class MockUnitOfWork extends UnitOfWork[IO]:
    override def atomic[A](action: IO[A])(using F: MonadCancelThrow[IO]): IO[A] =
      action

  test("onboardTenant should save tenant and outbox event") {
    val tenantRepo = new MockTenantRepository
    val outboxRepo = new MockOutboxRepository
    val uow = new MockUnitOfWork

    val service = new TenantServiceLive[IO](tenantRepo, outboxRepo, uow)

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
      assert(event.payload.noSpaces.contains("Test Tenant"))
    }
  }

  test("getTenant should return tenant from repository") {
    val tenantRepo = new MockTenantRepository
    val outboxRepo = new MockOutboxRepository
    val uow = new MockUnitOfWork

    val tenantId = TenantId.generate
    val tenant = Tenant(
      id = tenantId,
      name = "Existing Tenant",
      reportingCurrency = com.expatledger.kernel.domain.model.Currency("EUR"),
      taxResidencies = Set.empty,
      createdAt = OffsetDateTime.now(),
      updatedAt = OffsetDateTime.now()
    )
    tenantRepo.tenantsById = Map(tenantId -> tenant)

    val service = new TenantServiceLive[IO](tenantRepo, outboxRepo, uow)

    service.getTenant(tenantId).map { result =>
      assertEquals(result, Some(tenant))
    }
  }
