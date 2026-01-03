package com.expatledger.tenants.application

import cats.effect.*
import cats.syntax.all.*
import com.expatledger.kernel.application.EventPublisher
import com.expatledger.kernel.domain.OutboxRepository
import com.expatledger.tenants.config.OutboxConfig
import fs2.Stream

class OutboxPoller[F[_]: Async](
    outboxRepo: OutboxRepository[F],
    publisher: EventPublisher[F],
    config: OutboxConfig
):
  def run: Stream[F, Unit] =
    Stream
      .fixedDelay[F](config.pollInterval)
      .evalMap { _ =>
        outboxRepo.fetchUnprocessed(config.batchSize).flatMap { events =>
          if events.isEmpty then Async[F].unit
          else
            events
              .traverse { event =>
                publisher.publish(event).as(Some(event.id)).handleErrorWith { _ =>
                  Async[F].pure(None) // Tracked in T1.22: Better error handling/logging
                }
              }
              .flatMap { publishedIds =>
                val idsToMark = publishedIds.flatten
                if idsToMark.nonEmpty then outboxRepo.markProcessed(idsToMark)
                else Async[F].unit
              }
        }
      }
      .handleErrorWith { e =>
        Stream.exec(Async[F].delay(println(s"Error in outbox poller: ${e.getMessage}"))) ++ run // Restart on error
      }
