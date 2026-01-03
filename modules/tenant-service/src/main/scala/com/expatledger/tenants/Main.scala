package com.expatledger.tenants

import cats.effect.*
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.{Metadata, ServerServiceDefinition}

import java.net.InetSocketAddress
import com.expatledger.tenant.v1.tenant.*
import com.expatledger.tenants.config.TenantServiceConfig
import com.expatledger.tenants.infrastructure.persistence.DbMigrator
import skunk.Session
import natchez.Trace.Implicits.noop
import pureconfig.ConfigSource

object Main extends IOApp {

  private def loadConfig: IO[TenantServiceConfig] =
    IO.blocking(ConfigSource.default.loadOrThrow[TenantServiceConfig])

  def run(args: List[String]): IO[ExitCode] = {
    for {
      config <- loadConfig
      _      <- DbMigrator.migrate[IO](config.db)
      _      <- IO.println(s"Database migrations completed.")

      sessionPool = Session.pooled[IO](
        host = config.db.host,
        port = config.db.port,
        user = config.db.user,
        database = config.db.name,
        password = config.db.password,
        max = 10
      )

      _ <- sessionPool.use { pool =>
        pool.use { session =>

          val grpcService: TenantServiceFs2Grpc[IO, Metadata] = (request: GetTenantRequest, ctx: Metadata) =>
            IO.pure(GetTenantResponse(id = request.id, name = "Mock Tenant"))

          val serviceDefinition: Resource[IO, ServerServiceDefinition] =
            TenantServiceFs2Grpc.bindServiceResource[IO](grpcService)

          serviceDefinition.use { service =>
            IO.blocking {
              NettyServerBuilder
                .forAddress(new InetSocketAddress(config.host.toString, config.port.value))
                .addService(service)
                .build()
                .start()
            } *> IO.println(s"Tenant Service started on ${config.host}:${config.port}") *> IO.never
          }
        }
      }
    } yield ExitCode.Success
  }
}
