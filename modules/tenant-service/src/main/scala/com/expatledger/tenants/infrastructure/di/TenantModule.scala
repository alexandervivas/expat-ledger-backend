package com.expatledger.tenants.infrastructure.di

import com.expatledger.kernel.domain.OutboxRepository
import com.expatledger.tenants.application.{TenantService, TenantServiceLive, UnitOfWork}
import skunk.Session
import cats.effect.IO
import com.expatledger.tenants.domain.repositories.TenantRepository
import com.expatledger.tenants.infrastructure.persistence.{SkunkOutboxRepository, SkunkTenantRepository, SkunkUnitOfWork}
import com.google.inject.{AbstractModule, TypeLiteral}

class TenantModule(session: Session[IO]) extends AbstractModule {
  override def configure(): Unit = {
    bind(new TypeLiteral[TenantRepository[IO]] {}).toInstance(new SkunkTenantRepository[IO](session))
    bind(new TypeLiteral[OutboxRepository[IO]] {}).toInstance(new SkunkOutboxRepository[IO](session))
    bind(new TypeLiteral[UnitOfWork[IO]] {}).toInstance(new SkunkUnitOfWork[IO](session))
    val _ = bind(new TypeLiteral[TenantService[IO]] {}).to(new TypeLiteral[TenantServiceLive[IO]] {})
  }
}
