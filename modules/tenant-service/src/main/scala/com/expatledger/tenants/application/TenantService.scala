package com.expatledger.tenants.application

import com.expatledger.tenants.domain.model.{Tenant, TenantId}

trait TenantService[F[_]]:
  def onboardTenant(request: OnboardTenantRequest): F[TenantId]
  def getTenant(id: TenantId): F[Option[Tenant]]
