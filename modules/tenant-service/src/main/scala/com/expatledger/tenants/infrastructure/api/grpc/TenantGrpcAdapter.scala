package com.expatledger.tenants.infrastructure.api.grpc

import cats.effect.*
import cats.syntax.all.*
import com.expatledger.tenant.v1.tenant.{GetTenantResponse, OnboardTenantResponse, TenantServiceFs2Grpc, GetTenantRequest as GetTenantRequestGrpc, OnboardTenantRequest as OnboardTenantRequestGrpc}
import io.grpc.{Metadata, Status, StatusRuntimeException}
import com.expatledger.tenants.application.{OnboardTenantRequest, TenantService}
import com.expatledger.tenants.domain.model.TenantId

import java.util.UUID

class TenantGrpcAdapter[F[_] : Async](tenantService: TenantService[F]) extends TenantServiceFs2Grpc[F, Metadata] {

  override def getTenant(request: GetTenantRequestGrpc, ctx: Metadata): F[GetTenantResponse] = {
    val tenantId = TenantId(UUID.fromString(request.id))
    tenantService.getTenant(tenantId).flatMap {
      case Some(tenant) =>
        Async[F].pure(GetTenantResponse(id = tenant.id.toString, name = tenant.name))
      case None =>
        Async[F].raiseError(new StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(s"Tenant with id ${request.id} not found")))
    }
  }

  override def onboardTenant(request: OnboardTenantRequestGrpc, ctx: Metadata): F[OnboardTenantResponse] = {
    val appRequest = OnboardTenantRequest(
      name = request.name,
      reportingCurrency = request.reportingCurrency,
      initialTaxResidency = request.initialTaxResidency
    )
    tenantService.onboardTenant(appRequest).map { id =>
      OnboardTenantResponse(id = id.toString)
    }.recoverWith {
      case e: IllegalArgumentException =>
        Async[F].raiseError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage)))
      case e =>
        Async[F].raiseError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage)))
    }
  }
}
