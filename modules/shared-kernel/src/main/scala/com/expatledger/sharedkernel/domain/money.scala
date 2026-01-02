package com.expatledger.sharedkernel.domain

import scala.annotation.targetName

object money:
  opaque type Currency <: String = String
  object Currency:
    def apply(value: String): Currency = value.toUpperCase
    def unsafe(value: String): Currency = value

  opaque type Amount <: BigDecimal = BigDecimal
  object Amount:
    def apply(value: BigDecimal): Amount = value.setScale(2, BigDecimal.RoundingMode.HALF_UP)
    def fromDouble(value: Double): Amount = apply(BigDecimal(value))
    def fromLong(value: Long): Amount = apply(BigDecimal(value))

  case class Money(amount: Amount, currency: Currency):
    @targetName("add")
    def +(other: Money): Either[String, Money] =
      if currency == other.currency then
        Right(Money(Amount(amount + other.amount), currency))
      else
        Left(s"Cannot add different currencies: $currency and ${other.currency}")

    @targetName("subtract")
    def -(other: Money): Either[String, Money] =
      if currency == other.currency then
        Right(Money(Amount(amount - other.amount), currency))
      else
        Left(s"Cannot subtract different currencies: $currency and ${other.currency}")

    @targetName("multiply")
    def *(multiplier: BigDecimal): Money =
      Money(Amount(amount * multiplier), currency)

  object Money:
    def usd(amount: BigDecimal): Money = Money(Amount(amount), Currency("USD"))
    def eur(amount: BigDecimal): Money = Money(Amount(amount), Currency("EUR"))
    def cop(amount: BigDecimal): Money = Money(Amount(amount), Currency("COP"))
