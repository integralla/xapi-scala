package io.integralla.xapi.model

import io.circe.generic.auto._
import io.integralla.xapi.model.common.{Decodable, Encodable}
import io.integralla.xapi.model.exceptions.StatementValidationException
import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

class MBoxTest extends AnyFunSpec {

  case class Wrapper(mbox: MBox) extends Encodable[Wrapper]
  object Wrapper extends Decodable[Wrapper]

  describe("An MBox") {

    describe("[validation]") {
      it("should throw a validation exception if the mbox value is not a valid mailto IRI") {
        val data: String = """{"mbox": "info@example.com"}""".stripMargin
        assertThrows[StatementValidationException] {
          Wrapper(data)
        }
      }
    }

    describe("[encoding]") {
      it("should support encoding as JSON") {
        val wrapper: Wrapper = Wrapper(MBox("mailto:info@example.com"))
        val actual: String = wrapper.toJson()
        val expected = """{"mbox":"mailto:info@example.com"}""".stripMargin
        assert(actual === expected)
      }
    }

    describe("[decoding]") {
      it("should support decoding from JSON") {
        val data: String = """{"mbox": "mailto:info@example.com"}""".stripMargin
        val decoded: Try[Wrapper] = Wrapper(data)
        val expected = Wrapper(MBox("mailto:info@example.com"))

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should fail on decoding JSON where the value of mbox is the wrong type") {
        val data: String = """{"mbox": 1}""".stripMargin
        val decoded: Try[Wrapper] = Wrapper(data)
        assert(decoded.isFailure)
      }
    }

    describe("[equivalence]") {
      it("should return true if both mbox values match") {
        val left = MBox("mailto:populus.tremuloides@integralla.io")
        val right = MBox("mailto:populus.tremuloides@integralla.io")
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both mbox values match except the schema case") {
        val left = MBox("mailto:populus.tremuloides@integralla.io")
        val right = MBox("MAILTO:populus.tremuloides@integralla.io")
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both mbox values match except the email case") {
        val left = MBox("mailto:populus.tremuloides@integralla.io")
        val right = MBox("mailto:POPULUS.TREMULOIDES@INTEGRALLA.IO")
        assert(left.isEquivalentTo(right))
      }
      it("should return false if the mbox values don't match") {
        val left = MBox("mailto:populus.tremuloides@integralla.io")
        val right = MBox("mailto:populus.tremuloides@integralla.com")
        assert(left.isEquivalentTo(right) === false)
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
