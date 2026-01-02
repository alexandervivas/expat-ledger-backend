package com.expatledger.tenantservice

import cats.effect.*
import com.comcast.ip4s.*
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.{Metadata, ServerServiceDefinition}
import java.net.InetSocketAddress
import com.expatledger.tenant.v1.tenant.*

object Main extends IOApp {

  private val tenantService: TenantServiceFs2Grpc[IO, Metadata] = (request: GetTenantRequest, ctx: Metadata) =>
    IO.pure(GetTenantResponse(id = request.id, name = "Mock Tenant"))

  def run(args: List[String]): IO[ExitCode] = {
    val host = Host.fromString(sys.env.getOrElse("HOST", "0.0.0.0")).getOrElse(host"0.0.0.0")
    val port = Port.fromString(sys.env.getOrElse("PORT", "9000")).getOrElse(port"9000")

    val serviceDefinition: Resource[IO, ServerServiceDefinition] =
      TenantServiceFs2Grpc.bindServiceResource[IO](tenantService)

    serviceDefinition.use { service =>
      IO.blocking {
        NettyServerBuilder
          .forAddress(new InetSocketAddress(host.toString, port.value))
          .addService(service)
          .build()
          .start()
      } *> IO.println(s"Tenant Service started on $host:$port") *> IO.never
    }.as(ExitCode.Success)
  }
}
