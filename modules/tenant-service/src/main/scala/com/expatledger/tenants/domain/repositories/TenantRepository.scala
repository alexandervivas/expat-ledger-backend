package com.expatledger.tenants.domain.repositories

import com.expatledger.tenants.domain.model.{Tenant, TenantId}

trait TenantRepository[F[_]]:
  def save(tenant: Tenant): F[Unit]
  def findById(id: TenantId): F[Option[Tenant]]
