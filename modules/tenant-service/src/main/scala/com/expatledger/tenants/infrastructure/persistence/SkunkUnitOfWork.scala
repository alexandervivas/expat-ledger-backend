package com.expatledger.tenants.infrastructure.persistence

import cats.effect.*
import com.expatledger.tenants.application.UnitOfWork
import skunk.*

class SkunkUnitOfWork[F[_]](pool: Resource[F, Session[F]]) extends UnitOfWork[F]:
  override def atomic[A](action: F[A])(using F: MonadCancelThrow[F]): F[A] =
    pool.use(_.transaction.use(_ => action))
