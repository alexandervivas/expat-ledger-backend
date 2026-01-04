package com.expatledger.tenants

import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import io.grpc.{Metadata, ServerServiceDefinition}
import java.net.InetSocketAddress
import _root_.com.expatledger.tenant.v1.tenant.*
import com.expatledger.kernel.domain.repositories.OutboxRepository
import com.expatledger.tenants.config.{RabbitMQConfig, TenantServiceConfig}
import com.expatledger.tenants.infrastructure.persistence.{DbMigrator, OutboxRepositoryLive, SkunkUnitOfWork, TenantRepositoryLive}
import com.expatledger.tenants.infrastructure.api.grpc.TenantGrpcAdapter
import com.expatledger.tenants.application.TenantServiceLive
import skunk.Session
import natchez.Trace.Implicits.noop
import pureconfig.ConfigSource
import com.expatledger.tenants.infrastructure.messaging.RabbitMQPublisher
import dev.profunktor.fs2rabbit.model.*
import dev.profunktor.fs2rabbit.config.*
import com.expatledger.tenants.application.OutboxPoller
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.effects.MessageEncoder

import scala.concurrent.duration.*

object Main extends IOApp {

  private def loadConfig: IO[TenantServiceConfig] =
    IO.blocking(ConfigSource.default.loadOrThrow[TenantServiceConfig])

  implicit val stringMessageEncoder: MessageEncoder[IO, AmqpMessage[Array[Byte]]] =
    Kleisli[IO, AmqpMessage[Array[Byte]], AmqpMessage[Array[Byte]]](IO.pure)

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

  private def runGrpcServer(config: TenantServiceConfig, grpcService: TenantServiceFs2Grpc[IO, Metadata]): IO[Unit] = {
    val serviceDefinition: Resource[IO, ServerServiceDefinition] =
      TenantServiceFs2Grpc.bindServiceResource[IO](grpcService)

    serviceDefinition.use { service =>
      IO.blocking {
        NettyServerBuilder
          .forAddress(new InetSocketAddress(config.host.toString, config.port.value))
          .addService(service)
          .build()
          .start()
      } *> IO.println(s"Tenant gRPC Service started on ${config.host}:${config.port}") *> IO.never
    }
  }

  private def runOutboxPoller(config: TenantServiceConfig, outboxRepo: OutboxRepository[IO]): IO[Unit] = {
    setupRabbitMQ(config.rabbitmq).use { rabbit =>
      val exchangeName = ExchangeName(config.rabbitmq.exchange)
      val routingKey = RoutingKey(config.rabbitmq.routingKey)
      val queueName = QueueName(s"${config.rabbitmq.exchange}.outbox")

      rabbit.createConnectionChannel.use { implicit channel =>
        for {
          _ <- rabbit.declareQueue(DeclarationQueueConfig.default(queueName))
          _ <- rabbit.declareExchange(exchangeName, ExchangeType.Topic)
          _ <- rabbit.bindQueue(queueName, exchangeName, routingKey)
          amqpPublisher <- rabbit.createPublisher[AmqpMessage[Array[Byte]]](exchangeName, routingKey)

          eventPublisher = new RabbitMQPublisher[IO](amqpPublisher)
          poller = new OutboxPoller[IO](outboxRepo, eventPublisher, config.outbox)

          _ <- IO.println(s"Outbox Poller started with interval ${config.outbox.pollInterval}")
          _ <- poller.run.compile.drain
        } yield ()
      }
    }
  }

  def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- IO.println(s"Loading config...")
      config <- loadConfig
      _ <- IO.println(s"Config loaded.")
      _ <- IO.println(s"Database migrations starting...")
      _ <- DbMigrator.migrate[IO](config.db)
      _ <- IO.println(s"Database migrations completed.")

      _ <- sessionPool(config).use { pool =>
        val tenantRepo = new TenantRepositoryLive[IO](pool)
        val outboxRepo = new OutboxRepositoryLive[IO](pool)
        val uow = new SkunkUnitOfWork[IO](pool)
        val tenantService = new TenantServiceLive[IO](tenantRepo, outboxRepo, uow)
        val grpcService = new TenantGrpcAdapter[IO](tenantService)

        val mode = args.headOption.getOrElse("all")

        mode match {
          case "grpc" => runGrpcServer(config, grpcService)
          case "poller" => runOutboxPoller(config, outboxRepo)
          case "all" =>
            IO.println("Starting both gRPC server and Outbox Poller...") *>
            (runGrpcServer(config, grpcService), runOutboxPoller(config, outboxRepo)).parTupled.void
          case _ => IO.raiseError(new IllegalArgumentException(s"Unknown mode: $mode. Use 'grpc', 'poller', or 'all'."))
        }
      }
    } yield ExitCode.Success
  }

  private def sessionPool(config: TenantServiceConfig): Resource[IO, Resource[IO, Session[IO]]] = Session.pooled[IO](
    host = config.db.host,
    port = config.db.port,
    user = config.db.user,
    database = config.db.name,
    password = config.db.password,
    max = 10
  )
}
