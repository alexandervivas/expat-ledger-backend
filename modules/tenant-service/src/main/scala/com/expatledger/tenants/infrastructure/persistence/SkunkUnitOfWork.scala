package com.expatledger.tenants.infrastructure.persistence

import cats.effect.MonadCancelThrow
import com.expatledger.tenants.application.UnitOfWork
import skunk.Session

class SkunkUnitOfWork[F[_]](session: Session[F]) extends UnitOfWork[F]:
  override def atomic[A](action: F[A])(using F: MonadCancelThrow[F]): F[A] =
    session.transaction.use(_ => action)
