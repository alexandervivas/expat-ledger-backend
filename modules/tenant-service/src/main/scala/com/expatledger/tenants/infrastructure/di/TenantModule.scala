package com.expatledger.tenants.infrastructure.di

import _root_.com.expatledger.tenant.v1.tenant.TenantServiceFs2Grpc
import cats.effect.*
import com.expatledger.kernel.domain.repositories.OutboxRepository
import com.expatledger.tenants.application.{TenantService, TenantServiceLive, UnitOfWork}
import skunk.*
import io.grpc.Metadata
import com.expatledger.tenants.domain.repositories.TenantRepository
import com.expatledger.tenants.infrastructure.api.grpc.TenantGrpcAdapter
import com.expatledger.tenants.infrastructure.persistence.{OutboxRepositoryLive, SkunkUnitOfWork, TenantRepositoryLive}
import com.google.inject.{AbstractModule, TypeLiteral}

class TenantModule[F[_] : Sync](pool: Resource[F, Session[F]]) extends AbstractModule {
  override def configure(): Unit = {
    bind(new TypeLiteral[TenantRepository[F]] {}).toInstance(new TenantRepositoryLive[F](pool))
    bind(new TypeLiteral[OutboxRepository[F]] {}).toInstance(new OutboxRepositoryLive[F](pool))
    bind(new TypeLiteral[UnitOfWork[F]] {}).toInstance(new SkunkUnitOfWork[F](pool))
    val _ = bind(new TypeLiteral[TenantService[F]] {}).to(new TypeLiteral[TenantServiceLive[F]] {})
    val _ = bind(new TypeLiteral[TenantServiceFs2Grpc[F, Metadata]] {}).to(new TypeLiteral[TenantGrpcAdapter[F]] {})
  }
}
