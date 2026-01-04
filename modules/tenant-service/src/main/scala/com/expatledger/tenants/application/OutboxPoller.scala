package com.expatledger.tenants.application

import cats.effect.*
import cats.syntax.all.*
import com.expatledger.kernel.application.EventPublisher
import com.expatledger.kernel.domain.repositories.OutboxRepository
import com.expatledger.tenants.config.OutboxConfig
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration.*

class OutboxPoller[F[_]](
    outboxRepo: OutboxRepository[F],
    publisher: EventPublisher[F],
    config: OutboxConfig
)(using F: Async[F]):
  private val logger: Logger[F] = Slf4jLogger.getLoggerFromName[F]("OutboxPoller")

  private def retry[A](fa: F[A], delay: FiniteDuration, retries: Int): F[A] =
    fa.handleErrorWith { e =>
      if retries > 0 then F.sleep(delay) >> retry(fa, delay * 2, retries - 1)
      else F.raiseError(e)
    }

  def run: Stream[F, Unit] =
    Stream
      .fixedDelay[F](config.pollInterval)
      .evalMap { _ =>
        outboxRepo.fetchUnprocessed(config.batchSize).flatMap { events =>
          if events.isEmpty then F.unit
          else
            events
              .traverse { event =>
                retry(publisher.publish(event), config.retryInitialDelay, config.retryCount)
                  .as(Some(event.id))
                  .handleErrorWith { e =>
                    logger.error(e)(s"Failed to publish event ${event.id} after retries").as(None)
                  }
              }
              .flatMap { publishedIds =>
                val idsToMark = publishedIds.flatten
                if idsToMark.nonEmpty then outboxRepo.markProcessed(idsToMark)
                else F.unit
              }
        }
      }
      .handleErrorWith { e =>
        Stream.eval(logger.error(e)("Error in outbox poller")).drain
      }
      .repeat
