package com.expatledger.tenants.infrastructure.api.grpc

import cats.effect.*
import cats.syntax.all.*
import io.grpc.Metadata
import _root_.com.expatledger.tenant.v1.tenant.*
import com.expatledger.tenants.application.TenantService
import com.expatledger.tenants.domain.model.TenantId
import jakarta.inject.Inject
import java.util.UUID

class TenantGrpcAdapter[F[_]: Async] @Inject()(tenantService: TenantService[F]) extends TenantServiceFs2Grpc[F, Metadata] {

  override def getTenant(request: GetTenantRequest, ctx: Metadata): F[GetTenantResponse] = {
    val tenantId = TenantId(UUID.fromString(request.id))
    tenantService.getTenant(tenantId).flatMap {
      case Some(tenant) => 
        Async[F].pure(GetTenantResponse(id = tenant.id.toString, name = tenant.name))
      case None => 
        Async[F].raiseError(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(s"Tenant with id ${request.id} not found")))
    }
  }
}
