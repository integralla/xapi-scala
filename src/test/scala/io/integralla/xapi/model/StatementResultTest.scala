package io.integralla.xapi.model

import com.typesafe.scalalogging.StrictLogging
import io.circe.jawn.decode
import io.circe.parser._
import io.circe.syntax.EncoderOps
import io.integralla.xapi.model.exceptions.StatementValidationException
import org.scalatest.funspec.AnyFunSpec

class StatementResultTest extends AnyFunSpec with StrictLogging {

  val sampleScore: Score = Score(Some(0.5), Some(5.0), Some(0.0), Some(10.0))
  val sampleExtensions: ExtensionMap = ExtensionMap(
    Map(IRI("https://example.com/extenions/other") -> parse("""{"one": 1, "two": 2}""").toOption.get)
  )

  val sampleResult: StatementResult = StatementResult(
    Some(sampleScore),
    Some(true),
    Some(true),
    Some("response"),
    Some("PT4H35M59.14S"),
    Some(sampleExtensions)
  )

  val sampleResultEncoded: String =
    """{"score":{"scaled":0.5,"raw":5.0,"min":0.0,"max":10.0},"success":true,"completion":true,"response":"response","duration":"PT4H35M59.14S","extensions":{"https://example.com/extenions/other":{"one":1,"two":2}}}"""

  describe("StatementResult") {
    describe("[validation]") {
      it("should not throw a statement validation error for a valid duration [time]") {
        StatementResult(
          Some(sampleScore),
          Some(true),
          Some(true),
          Some("response"),
          Some("PT16559.14S"),
          Some(sampleExtensions)
        )
      }

      it("should not throw a statement validation error for a valid duration [period]") {
        StatementResult(
          Some(sampleScore),
          Some(true),
          Some(true),
          Some("response"),
          Some("P4W"),
          Some(sampleExtensions)
        )
      }

      it("should not throw a statement validation error for a valid duration [period + time]") {
        StatementResult(
          Some(sampleScore),
          Some(true),
          Some(true),
          Some("response"),
          Some("P3Y1M29DT4H35M59.14S"),
          Some(sampleExtensions)
        )
      }

      it("should throw a statement validation error for a invalid duration") {
        val exception = intercept[StatementValidationException] {
          StatementResult(
            Some(sampleScore),
            Some(true),
            Some(true),
            Some("response"),
            Some("T15M20.345S"),
            Some(sampleExtensions)
          )
        }
        assert(
          exception.getMessage.contains(
            "The supplied duration could not be parsed: duration(T15M20.345S), errorIndex(0)"
          )
        )
      }
    }

    describe("[encoding]") {
      it("should successfully encode a result") {
        logger.info(s"SAMPLE: $sampleResult")
        val actual: String = sampleResult.asJson.noSpaces
        logger.info(s"Encoded: $actual")
        logger.info(s"Expected: $sampleResultEncoded")
        assert(actual === sampleResultEncoded)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a result") {
        val decoded: Either[io.circe.Error, StatementResult] = decode[StatementResult](sampleResultEncoded)
        decoded match {
          case Right(actual) =>
            logger.info(s"Decoded: $actual")
            assert(actual === sampleResult)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a result in which some values are not set") {
        val data: String =
          """{"score":{"scaled":0.5,"raw":5.0,"min":0.0,"max":10.0},"success":true,"completion":true,"duration":"PT4H35M59.14S"}"""
        val decoded: Either[io.circe.Error, StatementResult] = decode[StatementResult](data)
        val expected: StatementResult = StatementResult(
          Some(sampleScore),
          Some(true),
          Some(true),
          None,
          Some("PT4H35M59.14S"),
          None
        )
        decoded match {
          case Right(actual) =>
            logger.info(s"Decoded: $actual")
            assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }
    }

    describe("[equivalence]") {
      it("should return true if both results are equivalent") {
        val left: StatementResult = StatementResult(
          Some(sampleScore),
          Some(true),
          Some(true),
          Some("response"),
          Some("PT4H35M59.14S"),
          Some(sampleExtensions)
        )

        val right: StatementResult = left.copy()
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both results are equivalent, excepting duration precision") {
        val left: StatementResult = StatementResult(
          Some(sampleScore),
          Some(true),
          Some(true),
          Some("response"),
          Some("PT4H35M59.14S"),
          Some(sampleExtensions)
        )

        val right: StatementResult = left.copy(
          duration = Some("PT4H35M59.149S")
        )
        assert(left.isEquivalentTo(right))
      }

      it("should return false if both results are not equivalent") {
        val left: StatementResult = StatementResult(
          Some(sampleScore),
          Some(true),
          Some(true),
          Some("response"),
          Some("PT4H35M59.14S"),
          Some(sampleExtensions)
        )

        val right: StatementResult = left.copy(
          duration = Some("PT16559.14S")
        )
        assert(left.isEquivalentTo(right) === false)
      }
    }
  }
}
