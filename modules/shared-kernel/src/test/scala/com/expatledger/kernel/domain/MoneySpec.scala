package com.expatledger.kernel.domain

import munit.FunSuite
import com.expatledger.kernel.domain.*

class MoneySpec extends FunSuite {

  test("Money addition with same currency") {
    val m1 = Money.usd(100.00)
    val m2 = Money.usd(50.50)
    val result = m1 + m2
    assertEquals(result, Right(Money.usd(150.50)))
  }

  test("Money addition with different currencies should fail") {
    val m1 = Money.usd(100.00)
    val m2 = Money.eur(50.50)
    val result = m1 + m2
    assert(result.isLeft)
  }

  test("Money subtraction with same currency") {
    val m1 = Money.usd(100.00)
    val m2 = Money.usd(50.50)
    val result = m1 - m2
    assertEquals(result, Right(Money.usd(49.50)))
  }

  test("Money multiplication") {
    val m1 = Money.usd(100.00)
    val result = m1 * 1.5
    assertEquals(result, Money.usd(150.00))
  }

  test("Amount rounding") {
    val amount = Amount(100.555)
    assertEquals(amount: BigDecimal, BigDecimal(100.56))
  }

  test("Currency normalization") {
    val c = Currency("usd")
    assertEquals(c: String, "USD")
  }
}
