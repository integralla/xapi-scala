package io.integralla.xapi.model.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.xapi.model.exceptions.StatementValidationException
import org.scalatest.funspec.AnyFunSpec

class ScoreTest extends AnyFunSpec {

  describe("Score") {
    describe("[validation]") {
      it("should not throw a statement validation error if the scaled score has been normalized (-1)") {
        Score(Some(-1.0), None, None, None)
      }

      it("should not throw a statement validation error if the scaled score has been normalized (1)") {
        Score(Some(1.0), None, None, None)
      }

      it("should throw a statement validation error if the scaled score is less than -1") {
        val exception = intercept[StatementValidationException] {
          Score(Some(-1.1), None, None, None)
        }
        assert(exception.getMessage.contains("A scaled score must be a normalized value between -1 and 1, inclusive"))
      }

      it("should throw a statement validation error if the scaled score is greater than 1") {
        val exception = intercept[StatementValidationException] {
          Score(Some(1.1), None, None, None)
        }
        assert(exception.getMessage.contains("A scaled score must be a normalized value between -1 and 1, inclusive"))
      }

      it("should throw a statement validation error if the raw score is less than the defined min score") {
        val exception = intercept[StatementValidationException] {
          Score(None, Some(-0.1), Some(0.0), Some(1.0))
        }
        assert(
          exception.getMessage.contains(
            "The raw score cannot be less than the lowest possible (min) score defined for the experience"
          )
        )
      }

      it("should throw a statement validation error if the raw score is greater than the defined max score") {
        val exception = intercept[StatementValidationException] {
          Score(None, Some(100.0), Some(0.0), Some(1.0))
        }
        assert(
          exception.getMessage.contains(
            "The raw score cannot be greater than the highest possible (max) score defined for the experience"
          )
        )
      }

      it(
        "should throw a statement validation error if the highest possible score (max) is less than the lowest possible score (min)"
      ) {
        val exception = intercept[StatementValidationException] {
          Score(None, None, Some(101.0), Some(100.0))
        }
        assert(
          exception.getMessage.contains(
            "The highest possible score (max) must be greater than the lowest possible score (min)"
          )
        )
      }
    }

    describe("[encoding]") {
      it("should successfully encode a score") {
        val score: Score = Score(Some(0.5), Some(5.0), Some(0.0), Some(10.0))
        val actual: String = score.asJson.noSpaces
        val expected: String = """{"scaled":0.5,"raw":5.0,"min":0.0,"max":10.0}"""
        assert(actual === expected)
      }

      it("should successfully encode a score in which some values are not set") {
        val score: Score = Score(Some(0.5), Some(5.0), None, None)
        val actual: String = score.asJson.noSpaces
        val expected: String = """{"scaled":0.5,"raw":5.0}"""
        assert(actual === expected)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a score") {
        val data: String = """{"scaled":0.5,"raw":5.0,"min":0.0,"max":10.0}"""
        val decoded: Either[io.circe.Error, Score] = decode[Score](data)
        val expected: Score = Score(Some(0.5), Some(5.0), Some(0.0), Some(10.0))
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a score in which some values are not set") {
        val data: String = """{"scaled":0.5,"raw":5.0}"""
        val decoded: Either[io.circe.Error, Score] = decode[Score](data)
        val expected: Score = Score(Some(0.5), Some(5.0), None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }
    }

    describe("[equivalence]") {
      val score: Score = Score(Some(0.5), Some(5.0), Some(0.0), Some(10.0))
      it("should return true if both scores are the same") {
        val left = score.copy()
        val right = score.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both scores are the same excepting trailing zeros") {
        val left = score.copy()
        val right = score.copy(
          scaled = Some(0.5000000)
        )
        assert(left.isEquivalentTo(right))
      }
      it("should return false if the scores are not the same") {
        val left = score.copy()
        val right = score.copy(min = None, max = None)
        assert(left.isEquivalentTo(right) === false)
      }
    }

  }
}
