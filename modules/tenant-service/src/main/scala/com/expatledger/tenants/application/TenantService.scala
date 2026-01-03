package com.expatledger.tenants.application

import com.expatledger.tenants.domain.model.TenantId

trait TenantService[F[_]]:
  def onboardTenant(request: OnboardTenantRequest): F[TenantId]
