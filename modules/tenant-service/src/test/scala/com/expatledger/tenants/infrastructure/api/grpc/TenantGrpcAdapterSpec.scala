package com.expatledger.tenants.infrastructure.api.grpc

import cats.effect.*
import com.expatledger.tenant.v1.tenant.*
import com.expatledger.tenants.application.TenantService
import com.expatledger.tenants.domain.model.*
import com.expatledger.kernel.domain.model.Currency
import io.grpc.Metadata
import munit.CatsEffectSuite

import java.time.OffsetDateTime
import java.util.UUID

class TenantGrpcAdapterSpec extends CatsEffectSuite {

  val mockTenantService: TenantService[IO] = new TenantService[IO] {
    override def onboardTenant(request: com.expatledger.tenants.application.OnboardTenantRequest): IO[TenantId] = {
      if (request.reportingCurrency == "INVALID") IO.raiseError(new IllegalArgumentException("Invalid currency"))
      else IO.pure(TenantId(UUID.randomUUID()))
    }
    override def getTenant(id: TenantId): IO[Option[Tenant]] = {
      if (id.toString == "00000000-0000-0000-0000-000000000000") {
        IO.pure(Some(Tenant(id, "Test Tenant", Currency("USD"), Set.empty, OffsetDateTime.now(), OffsetDateTime.now())))
      } else {
        IO.pure(None)
      }
    }
  }

  val adapter = new TenantGrpcAdapter[IO](mockTenantService)

  test("getTenant should return response when tenant exists") {
    val id = "00000000-0000-0000-0000-000000000000"
    val request = GetTenantRequest(id = id)
    adapter.getTenant(request, new Metadata()).map { response =>
      assertEquals(response.id, id)
      assertEquals(response.name, "Test Tenant")
    }
  }

  test("getTenant should fail when tenant does not exist") {
    val id = UUID.randomUUID().toString
    val request = GetTenantRequest(id = id)
    adapter.getTenant(request, new Metadata()).intercept[io.grpc.StatusRuntimeException]
  }

  test("onboardTenant should return response") {
    val request = OnboardTenantRequest(
      name = "New Tenant",
      reportingCurrency = "USD",
      initialTaxResidency = "US"
    )
    adapter.onboardTenant(request, new Metadata()).map { response =>
      assert(response.id.nonEmpty)
    }
  }

  test("onboardTenant should fail with invalid currency") {
    val request = OnboardTenantRequest(
      name = "New Tenant",
      reportingCurrency = "INVALID",
      initialTaxResidency = "US"
    )
    adapter.onboardTenant(request, new Metadata()).intercept[io.grpc.StatusRuntimeException]
  }
}
