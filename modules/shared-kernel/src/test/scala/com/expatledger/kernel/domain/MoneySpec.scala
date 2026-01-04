package com.expatledger.kernel.domain

import munit.FunSuite
import com.expatledger.kernel.domain.model.{Amount, Currency, Money}

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
    assertEquals(result, Left("Cannot add different currencies: USD and EUR"))
  }

  test("Money subtraction with same currency") {
    val m1 = Money.usd(100.00)
    val m2 = Money.usd(50.50)
    val result = m1 - m2
    assertEquals(result, Right(Money.usd(49.50)))
  }

  test("Money subtraction with different currencies should fail") {
    val m1 = Money.usd(100.00)
    val m2 = Money.eur(50.50)
    val result = m1 - m2
    assertEquals(result, Left("Cannot subtract different currencies: USD and EUR"))
  }

  test("Money multiplication") {
    val m1 = Money.usd(100.00)
    val result = m1 * 1.5
    assertEquals(result, Money.usd(150.00))
  }

  test("Money COP creation") {
    val m = Money.cop(1000.00)
    assertEquals(m.currency, Currency("COP"))
    assertEquals(m.amount: BigDecimal, BigDecimal(1000.00))
  }

  test("Amount rounding") {
    val amount = Amount(100.555)
    assertEquals(amount: BigDecimal, BigDecimal(100.56))
  }

  test("Currency normalization") {
    val c = Currency("usd")
    assertEquals(c: String, "USD")
  }

  test("Currency codec") {
    import io.circe.syntax.*
    val c = Currency("eur")
    val json = c.asJson.noSpaces
    assertEquals(json, "\"EUR\"")
    val decoded = io.circe.parser.decode[Currency](json)
    assertEquals(decoded, Right(c))
  }

  test("Amount fromDouble and fromLong") {
    val a1 = Amount.fromDouble(100.5)
    assertEquals(a1: BigDecimal, BigDecimal(100.50).setScale(2))
    val a2 = Amount.fromLong(100)
    assertEquals(a2: BigDecimal, BigDecimal(100.00).setScale(2))
  }
}
