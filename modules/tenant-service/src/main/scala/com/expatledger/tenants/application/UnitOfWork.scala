package com.expatledger.tenants.application

import cats.effect.MonadCancelThrow

trait UnitOfWork[F[_]]:
  def atomic[A](action: F[A])(using F: MonadCancelThrow[F]): F[A]
