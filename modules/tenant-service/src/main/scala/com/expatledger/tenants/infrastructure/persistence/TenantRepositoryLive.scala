package com.expatledger.tenants.infrastructure.persistence

import cats.effect.*
import cats.syntax.all.*
import com.expatledger.kernel.domain.model.Currency
import com.expatledger.tenants.domain.model.{TaxResidency, Tenant, TenantId}
import com.expatledger.tenants.domain.repositories.TenantRepository
import skunk.*
import skunk.codec.all.*
import skunk.syntax.all.*

import java.time.OffsetDateTime

private object TenantRepositoryLive {

  private val tenantId: Codec[TenantId] = uuid.imap(TenantId.apply)(identity)
  private val currency: Codec[Currency] = varchar(3).imap(Currency.apply)(identity)

  private val codec: Codec[Tenant] =
    (tenantId *: text *: currency *: timestamptz *: timestamptz *: EmptyTuple).tupled.imap {
      case id *: name *: reportingCurrency *: createdAt *: updatedAt *: EmptyTuple => Tenant(id, name, reportingCurrency, Set.empty, createdAt, updatedAt)
    }(tenant => tenant.id *: tenant.name *: tenant.reportingCurrency *: tenant.createdAt *: tenant.updatedAt *: EmptyTuple)

  private val insertTenant: Command[Tenant] =
    sql"""
         INSERT INTO tenant (id, name, reporting_currency, created_at, updated_at)
         VALUES ($codec)
       """.command

  private def insertTaxResidencies(size: Int): Command[List[TenantId *: String *: EmptyTuple]] =
    val encoder = (tenantId *: varchar(2) *: EmptyTuple).tupled.values.list(size)
    sql"INSERT INTO tenant_tax_residency (tenant_id, country_code) VALUES $encoder".command

  private val selectTenantById: Query[TenantId, Tenant] =
    sql"""
         SELECT id, name, reporting_currency, created_at, updated_at
         FROM tenant
         WHERE id = $tenantId
       """.query(codec)

  private val selectTaxResidenciesByTenantId: Query[TenantId, String] =
    sql"SELECT country_code FROM tenant_tax_residency WHERE tenant_id = $tenantId".query(varchar(2))

  def make[F[_] : Sync](pool: Resource[F, Session[F]]): TenantRepositoryLive[F] =
    new TenantRepositoryLive[F](pool)
}

class TenantRepositoryLive[F[_] : Sync](pool: Resource[F, Session[F]]) extends TenantRepository[F] {

  import TenantRepositoryLive.*

  override def findById(id: TenantId): F[Option[Tenant]] =
    pool.use { session =>
      for {
        tenantOpt <- session.option(selectTenantById)(id)
        taxResidencies <- tenantOpt.traverse(tenant => session.execute(selectTaxResidenciesByTenantId)(tenant.id))
      } yield buildTenant(tenantOpt, taxResidencies)
    }

  override def save(tenant: Tenant): F[Unit] = {
    val taxResidencies = tenant.taxResidencies.toList.map(tr => tenant.id *: tr.countryCode *: EmptyTuple)
    pool.use { session =>
      for {
        - <- session.execute(insertTenant)(tenant)
        _ <- if taxResidencies.nonEmpty then session
          .prepare(insertTaxResidencies(taxResidencies.length))
          .flatMap(command => command.execute(taxResidencies).void)
        else Sync[F].unit
      } yield ()
    }
  }

  private def buildTenant(maybeTenant: Option[Tenant], maybeStrings: Option[List[String]]): Option[Tenant] =
    maybeTenant.map(_.copy(taxResidencies = maybeStrings.getOrElse(List.empty).map(TaxResidency.apply).toSet))

}
