package io.integralla.xapi.model

import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.funspec.AnyFunSpec

class CorrectResponsePatternTest extends AnyFunSpec {
  describe("CorrectResponsePattern") {

    describe("[encoding]") {
      it("should encode a correct response pattern") {
        val crp: CorrectResponsePattern = CorrectResponsePattern(
          List(
            "quartz[,]crystal",
            "quartz"
          )
        )

        val encoded = crp.asJson.noSpacesSortKeys
        assert(encoded === """["quartz[,]crystal","quartz"]""")
      }

      it("should encode a correct response pattern that is an empty list") {
        val crp: CorrectResponsePattern = CorrectResponsePattern(List.empty[String])
        val encoded = crp.asJson.noSpacesSortKeys
        assert(encoded === """[]""")
      }
    }

    describe("[decoding]") {
      it("should decode a correct response pattern") {
        val crpStr: String = """["quartz[,]crystal","quartz"]"""
        val decoded: Either[io.circe.Error, CorrectResponsePattern] = decode[CorrectResponsePattern](crpStr)
        decoded match {
          case Right(actual) =>
            assert(actual.responses.length === 2)
            assert(actual.responses.contains("quartz[,]crystal"))
            assert(actual.responses.contains("quartz"))
          case Left(err) => throw err
        }
      }

      it("should decode a correct response pattern that is an empty list") {
        val crpStr: String = """[]"""
        val decoded: Either[io.circe.Error, CorrectResponsePattern] = decode[CorrectResponsePattern](crpStr)
        decoded match {
          case Right(actual) =>
            assert(actual.responses.isEmpty)
          case Left(err) => throw err
        }
      }

      it("should decode a correct response pattern that included characterizing parameters") {
        val crpStr: String = """["{case_matters=false}{lang=en}To store and provide access to learning experiences."]"""
        val decoded: Either[io.circe.Error, CorrectResponsePattern] = decode[CorrectResponsePattern](crpStr)
        decoded match {
          case Right(actual) =>
            assert(
              actual.responses.head === """{case_matters=false}{lang=en}To store and provide access to learning experiences."""
            )
          case Left(err) => throw err
        }
      }
    }

    describe("[equivalence]") {
      val sample: CorrectResponsePattern = CorrectResponsePattern(
        List(
          "quartz[,]crystal",
          "quartz"
        )
      )

      it("should return true if both patterns match") {
        val left: CorrectResponsePattern = sample.copy()
        val right: CorrectResponsePattern = sample.copy()
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both patterns match except response order") {
        val left: CorrectResponsePattern = sample.copy()
        val right: CorrectResponsePattern = sample.copy(
          responses = List(
            "quartz",
            "quartz[,]crystal"
          )
        )
        assert(left.isEquivalentTo(right))
      }

      it("should return false if patterns don't match") {
        val left: CorrectResponsePattern = sample.copy()
        val right: CorrectResponsePattern = sample.copy(
          responses = List(
            "quartz",
            "quartz[,]crystal",
            "crystal"
          )
        )
        assert(left.isEquivalentTo(right) === false)
      }
    }
  }
}
