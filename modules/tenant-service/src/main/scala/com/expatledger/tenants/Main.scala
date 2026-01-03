package com.expatledger.tenants

import cats.effect.{ExitCode, IO, IOApp, Resource}
import io.grpc.{Metadata, ServerServiceDefinition}

import java.net.InetSocketAddress
import _root_.com.expatledger.tenant.v1.tenant.*
import com.expatledger.tenants.config.{RabbitMQConfig, TenantServiceConfig}
import com.expatledger.tenants.infrastructure.di.TenantModule
import com.expatledger.tenants.infrastructure.persistence.DbMigrator
import com.google.inject.{Guice, Key, TypeLiteral}
import skunk.Session
import natchez.Trace.Implicits.noop
import pureconfig.ConfigSource
import com.expatledger.tenants.infrastructure.messaging.RabbitMQPublisher
import dev.profunktor.fs2rabbit.model.*
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import com.expatledger.tenants.application.OutboxPoller
import com.expatledger.kernel.application.EventPublisher
import com.expatledger.kernel.domain.OutboxRepository
import dev.profunktor.fs2rabbit.interpreter.RabbitClient

import scala.concurrent.duration.*

object Main extends IOApp {

  private def loadConfig: IO[TenantServiceConfig] =
    IO.blocking(ConfigSource.default.loadOrThrow[TenantServiceConfig])

  private def setupRabbitMQ(config: RabbitMQConfig): Resource[IO, RabbitClient[IO]] =
    val rabbitConfig = Fs2RabbitConfig(
      host = config.host,
      port = config.port,
      virtualHost = config.virtualHost,
      connectionTimeout = 3.seconds,
      ssl = false,
      username = Some(config.user),
      password = config.password,
      requeueOnNack = false,
      requeueOnReject = false,
      internalQueueSize = Some(500),
      automaticTopologyRecovery = true
    )
    RabbitClient.default[IO](rabbitConfig).resource

  def run(args: List[String]): IO[ExitCode] = {
    for {
      config <- loadConfig
      _ <- DbMigrator.migrate[IO](config.db)
      _ <- IO.println(s"Database migrations completed.")

      _ <- setupRabbitMQ(config.rabbitmq).use { rabbit =>
        rabbit.createConnectionChannel.use { implicit channel =>
          val exchangeName = ExchangeName(config.rabbitmq.exchange)
          val routingKey = RoutingKey(config.rabbitmq.routingKey)

          sessionPool(config).use { pool =>
            pool.use { session =>
              val injector = Guice.createInjector(new TenantModule(session))
              val grpcService = injector.getInstance(Key.get(new TypeLiteral[TenantServiceFs2Grpc[IO, Metadata]] {}))
              val outboxRepo = injector.getInstance(Key.get(new TypeLiteral[OutboxRepository[IO]] {}))

              val publisher: EventPublisher[IO] = new RabbitMQPublisher[IO](rabbit, exchangeName, routingKey)
              val poller = new OutboxPoller[IO](outboxRepo, publisher, config.outbox)

              val serviceDefinition: Resource[IO, ServerServiceDefinition] =
                TenantServiceFs2Grpc.bindServiceResource[IO](grpcService)

              serviceDefinition.use { service =>
                val startGrpc = IO.blocking {
                  io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
                    .forAddress(new InetSocketAddress(config.host.toString, config.port.value))
                    .addService(service)
                    .build()
                    .start()
                } *> IO.println(s"Tenant Service started on ${config.host}:${config.port}") *> IO.never

                val startPoller = poller.run.compile.drain

                startGrpc.race(startPoller).as(ExitCode.Success)
              }
            }
          }
        }
      }
    } yield ExitCode.Success
  }

  private def sessionPool(config: TenantServiceConfig) = Session.pooled[IO](
    host = config.db.host,
    port = config.db.port,
    user = config.db.user,
    database = config.db.name,
    password = config.db.password,
    max = 10
  )
}
