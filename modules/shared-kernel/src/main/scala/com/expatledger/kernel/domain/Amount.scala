package com.expatledger.kernel.domain

opaque type Amount <: BigDecimal = BigDecimal
object Amount:
  def apply(value: BigDecimal): Amount = value.setScale(2, BigDecimal.RoundingMode.HALF_UP)
  def fromDouble(value: Double): Amount = apply(BigDecimal(value))
  def fromLong(value: Long): Amount = apply(BigDecimal(value))
