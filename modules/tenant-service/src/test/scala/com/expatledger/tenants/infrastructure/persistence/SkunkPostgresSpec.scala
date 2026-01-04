package com.expatledger.tenants.infrastructure.persistence

import cats.effect.*
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainerForAll
import munit.CatsEffectSuite
import org.flywaydb.core.Flyway
import skunk.Session
import natchez.Trace.Implicits.noop
import org.testcontainers.utility.DockerImageName

trait SkunkPostgresSpec extends CatsEffectSuite with TestContainerForAll {

  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def(
    DockerImageName.parse("postgres:16-alpine")
  )

  def migrate(url: String, user: String, pass: String): Unit = {
    val _ = Flyway
      .configure()
      .dataSource(url, user, pass)
      .load()
      .migrate()
  }

  def sessionPool(container: PostgreSQLContainer): Resource[IO, Resource[IO, Session[IO]]] = {
    migrate(container.jdbcUrl, container.username, container.password)
    Session.pooled[IO](
      host = container.host,
      port = container.mappedPort(5432),
      user = container.username,
      database = container.databaseName,
      password = Some(container.password),
      max = 10,
      parameters = Map("TimeZone" -> "UTC")
    )
  }
}
