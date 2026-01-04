package com.expatledger.kernel.domain.model

import scala.annotation.targetName

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
