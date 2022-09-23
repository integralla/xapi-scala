package io.integralla.model.xapi.statement.identifiers

import io.circe.generic.auto._
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.testing.spec.UnitSpec

class MBoxTest extends UnitSpec {

  case class Wrapper(mbox: MBox)

  describe("An MBox") {

    it("should support decoding from JSON") {
      val data: String = """{"mbox": "mailto:info@example.com"}""".stripMargin
      val decoded: Either[io.circe.Error, Wrapper] = decode[Wrapper](data)
      val expected = Wrapper(MBox("mailto:info@example.com"))
      decoded match {
        case Right(actual) => assert(actual === expected)
        case Left(_) => false
      }
    }

    it("should fail on decoding JSON where the value of mbox is the wrong type") {
      val data: String = """{"mbox": 1}""".stripMargin
      val decoded: Either[io.circe.Error, Wrapper] = decode[Wrapper](data)
      val success = decoded match {
        case Right(_) => true
        case Left(_) => false
      }
      assert(success === false)
    }

    it("should support encoding as JSON") {
      val wrapper: Wrapper = Wrapper(MBox("mailto:info@example.com"))
      val actual: String = wrapper.asJson.noSpaces
      val expected = """{"mbox":"mailto:info@example.com"}""".stripMargin
      assert(actual === expected)
    }

    it("should throw a validation exception if the mbox value is not a valid mailto IRI") {
      val data: String = """{"mbox": "info@example.com"}""".stripMargin
      assertThrows[StatementValidationException] {
        decode[Wrapper](data)
      }
    }

    describe("shaChecksum") {
      it("should return a shasum of the value") {
        val mbox = MBox("mailto:info@example.com")
        val expected = "1f7ce1c565d9e4311df3e5d7e691993c57bbc67b"
        val actual = mbox.shaChecksum
        assert(actual === expected)
      }

      it("should return a shasum of the value (upper case scenario)") {
        val mbox = MBox("mailto:UPPER@EXAMPLE.COM")
        val expected = "cceb72428e456578f5af0b49952066f4d865ee99"
        val actual = mbox.shaChecksum
        assert(actual === expected)
      }
    }


  }
}
