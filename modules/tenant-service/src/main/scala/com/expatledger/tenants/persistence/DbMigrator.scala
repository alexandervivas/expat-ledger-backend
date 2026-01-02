package com.expatledger.tenants.persistence

import cats.effect.*
import cats.syntax.all.*
import com.expatledger.tenants.config.DatabaseConfig
import org.flywaydb.core.Flyway

object DbMigrator {
  def migrate[F[_]: Sync](config: DatabaseConfig): F[Unit] = {
    Sync[F].delay {
      val url = s"jdbc:postgresql://${config.host}:${config.port}/${config.name}"
      val flyway = Flyway.configure()
        .dataSource(url, config.user, config.password.orNull)
        .load()
      flyway.migrate()
    }.void
  }
}
