package io.integralla.xapi.model

import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

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

        val encoded: String = crp.toJson()
        assert(encoded === """["quartz[,]crystal","quartz"]""")
      }

      it("should encode a correct response pattern that is an empty list") {
        val crp: CorrectResponsePattern = CorrectResponsePattern(List.empty[String])
        val encoded: String = crp.toJson()
        assert(encoded === """[]""")
      }
    }

    describe("[decoding]") {
      it("should decode a correct response pattern") {
        val crpStr: String = """["quartz[,]crystal","quartz"]"""
        val decoded: Try[CorrectResponsePattern] = CorrectResponsePattern(crpStr)
        assert(decoded.isSuccess)
        assert(decoded.get.responses === List("quartz[,]crystal", "quartz"))
      }

      it("should decode a correct response pattern that is an empty list") {
        val crpStr: String = """[]"""
        val decoded: Try[CorrectResponsePattern] = CorrectResponsePattern(crpStr)
        assert(decoded.isSuccess)
        assert(decoded.get.responses.isEmpty)
      }

      it("should decode a correct response pattern that included characterizing parameters") {
        val crpStr: String =
          """["{case_matters=false}{lang=en}To store and provide access to learning experiences."]"""

        val decoded: Try[CorrectResponsePattern] = CorrectResponsePattern(crpStr)
        assert(decoded.isSuccess)
        assert(
          decoded.get.responses === List(
            """{case_matters=false}{lang=en}To store and provide access to learning experiences."""
          )
        )
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
