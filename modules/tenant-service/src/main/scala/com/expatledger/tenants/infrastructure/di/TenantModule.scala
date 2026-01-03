package com.expatledger.tenants.infrastructure.di

import com.expatledger.kernel.domain.OutboxRepository
import com.expatledger.tenant.v1.tenant.TenantServiceFs2Grpc
import com.expatledger.tenants.application.{TenantService, TenantServiceLive, UnitOfWork}
import skunk.Session
import cats.effect.IO
import io.grpc.Metadata
import com.expatledger.tenants.domain.repositories.TenantRepository
import com.expatledger.tenants.infrastructure.api.grpc.TenantGrpcAdapter
import com.expatledger.tenants.infrastructure.persistence.{SkunkOutboxRepository, SkunkTenantRepository, SkunkUnitOfWork}
import com.google.inject.{AbstractModule, TypeLiteral}

class TenantModule(session: Session[IO]) extends AbstractModule {
  override def configure(): Unit = {
    bind(new TypeLiteral[TenantRepository[IO]] {}).toInstance(new SkunkTenantRepository[IO](session))
    bind(new TypeLiteral[OutboxRepository[IO]] {}).toInstance(new SkunkOutboxRepository[IO](session))
    bind(new TypeLiteral[UnitOfWork[IO]] {}).toInstance(new SkunkUnitOfWork[IO](session))
    val _ = bind(new TypeLiteral[TenantService[IO]] {}).to(new TypeLiteral[TenantServiceLive[IO]] {})
    val _ = bind(new TypeLiteral[TenantServiceFs2Grpc[IO, Metadata]] {}).to(new TypeLiteral[TenantGrpcAdapter[IO]] {})
  }
}
