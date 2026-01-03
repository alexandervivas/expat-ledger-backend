package com.expatledger.tenants.persistence

import cats.effect.*
import cats.syntax.all.*
import java.time.OffsetDateTime
import com.expatledger.tenants.domain.{Tenant, TenantId, TaxResidency}
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import com.expatledger.kernel.domain.Currency

trait TenantRepository[F[_]]:
  def save(tenant: Tenant): F[Unit]
  def findById(id: TenantId): F[Option[Tenant]]

private object SkunkTenantRepository:
  val tenantId: Codec[TenantId] = uuid.imap(TenantId.apply)(identity)
  val currency: Codec[Currency] = varchar(3).imap(Currency.apply)(identity)

  val tenantEncoder: Encoder[Tenant] =
    (tenantId *: text *: currency *: timestamptz *: timestamptz).values.contramap { t =>
      t.id *: t.name *: t.reportingCurrency *: t.createdAt *: t.updatedAt *: EmptyTuple
    }

  val tenantDecoder: Decoder[TenantId *: String *: Currency *: OffsetDateTime *: OffsetDateTime *: EmptyTuple] =
    tenantId *: text *: currency *: timestamptz *: timestamptz

  val insertTenant: Command[Tenant] =
    sql"INSERT INTO tenant (id, name, reporting_currency, created_at, updated_at) VALUES $tenantEncoder".command

  val insertTaxResidency: Command[TenantId *: String *: EmptyTuple] =
    sql"INSERT INTO tenant_tax_residency (tenant_id, country_code) VALUES ($tenantId, ${varchar(2)})".command

  val selectTenant: Query[TenantId, TenantId *: String *: Currency *: OffsetDateTime *: OffsetDateTime *: EmptyTuple] =
    sql"SELECT id, name, reporting_currency, created_at, updated_at FROM tenant WHERE id = $tenantId".query(tenantDecoder)

  val selectTaxResidencies: Query[TenantId, String] =
    sql"SELECT country_code FROM tenant_tax_residency WHERE tenant_id = $tenantId".query(varchar(2))

class SkunkTenantRepository[F[_]: Sync](session: Session[F]) extends TenantRepository[F]:
  import SkunkTenantRepository.*

  override def save(tenant: Tenant): F[Unit] =
    for
      _ <- session.execute(insertTenant)(tenant)
      _ <- tenant.taxResidencies.toList.traverse { tr =>
        session.execute(insertTaxResidency)(tenant.id *: tr.countryCode *: EmptyTuple)
      }
    yield ()

  override def findById(id: TenantId): F[Option[Tenant]] =
    for
      tOpt <- session.option(selectTenant)(id)
      res <- tOpt.traverse { case tid *: name *: curr *: ca *: ua *: EmptyTuple =>
        session.execute(selectTaxResidencies)(tid).map { trs =>
          Tenant(tid, name, curr, trs.map(TaxResidency.apply).toSet, ca, ua)
        }
      }
    yield res
