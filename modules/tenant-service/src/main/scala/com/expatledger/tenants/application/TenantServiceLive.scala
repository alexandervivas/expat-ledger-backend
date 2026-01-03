package com.expatledger.tenants.application

import cats.effect.*
import cats.syntax.all.*
import com.expatledger.kernel.domain.{Currency, OutboxRepository}
import com.expatledger.tenants.domain.*
import com.expatledger.tenants.domain.events.TenantCreated
import com.expatledger.tenants.domain.model.{TaxResidency, Tenant, TenantId}
import com.expatledger.tenants.domain.repositories.TenantRepository
import jakarta.inject.Inject

import java.time.{OffsetDateTime, ZoneOffset}

class TenantServiceLive[F[_] : MonadCancelThrow : Sync] @Inject()(
  tenantRepo: TenantRepository[F],
  outboxRepo: OutboxRepository[F],
  uow: UnitOfWork[F]
) extends TenantService[F]:

  override def onboardTenant(request: OnboardTenantRequest): F[TenantId] =
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

    val outboxEvent = TenantCreated(tenant, List(request.initialTaxResidency), now).toOutboxEvent

    uow.atomic {
      for
        _ <- tenantRepo.save(tenant)
        _ <- outboxRepo.save(outboxEvent)
      yield tenantId
    }
