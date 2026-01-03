package com.expatledger.tenants.service

import java.util.UUID
import java.time.OffsetDateTime
import java.time.ZoneOffset
import cats.effect.*
import cats.syntax.all.*
import com.expatledger.tenants.domain.*
import com.expatledger.tenants.persistence.{TenantRepository, OutboxRepository, Repository}
import com.expatledger.kernel.domain.Currency
import io.circe.syntax.*


trait TenantService[F[_]]:
  def onboardTenant(request: OnboardTenantRequest): F[TenantId]

class TenantServiceLive[F[_]: MonadCancelThrow: Sync](
    tenantRepo: TenantRepository[F],
    outboxRepo: OutboxRepository[F],
    baseRepo: Repository[F]
) extends TenantService[F]:

  override def onboardTenant(request: OnboardTenantRequest): F[TenantId] =
    baseRepo.atomic { session =>
      val tenantId = TenantId.generate
      val now = OffsetDateTime.now(ZoneOffset.UTC)
      
      val tenant = Tenant(
        id = tenantId,
        name = request.name,
        reportingCurrency = Currency(request.reportingCurrency),
        taxResidencies = Set(TaxResidency(request.initialTaxResidency)),
        createdAt = now,
        updatedAt = now
      )

      val event = TenantCreated(
        id = UUID.randomUUID(),
        aggregateId = tenantId,
        name = tenant.name,
        reportingCurrency = tenant.reportingCurrency,
        taxResidencies = List(request.initialTaxResidency),
        occurredAt = now
      )

      val payload = event.asJson.noSpaces
      
      val outboxEvent = baseRepo.toOutboxEvent(event, "Tenant", payload)

      for
        _ <- tenantRepo.save(tenant)
        _ <- outboxRepo.save(outboxEvent)
      yield tenantId
    }
