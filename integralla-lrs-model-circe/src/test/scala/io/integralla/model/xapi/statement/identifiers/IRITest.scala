package io.integralla.model.xapi.statement.identifiers

import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.testing.spec.UnitSpec

import io.circe.generic.auto._
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps

class IRITest extends UnitSpec {

  case class Wrapper(id: IRI)

  describe("[validation]") {
    it("should validate a valid URL") {
      val instance = IRI("http://example.com/visited")
      assert(instance.value === "http://example.com/visited")
    }

    it("should validate a valid URL with international characters") {
      val instance = IRI("http://example.com/فعل/خواندن")
      assert(instance.value === "http://example.com/فعل/خواندن")
    }

    it("should validate a valid URN") {
      val instance = IRI("example:verb:visited")
      assert(instance.value === "example:verb:visited")
    }

    it("should validate a valid URN with international characters") {
      val instance = IRI("example:verb:visiterò")
      assert(instance.value === "example:verb:visiterò")
    }

    it("should not validate an invalid URI") {
      assertThrows[StatementValidationException] {
        IRI("visited")
      }
    }
  }

  describe("[encoding]") {
    it("should successfully encode an IRI") {
      val wrapper: Wrapper = Wrapper(IRI("http://example.com/visited"))
      val actual: String = wrapper.asJson.noSpaces
      val expected: String = """{"id":"http://example.com/visited"}"""
      assert(actual === expected)
    }
  }

  describe("[decoding]") {
    it("should successfully decode an IRI") {
      val data: String = """{"id":"http://example.com/visited"}"""
      val decoded: Either[io.circe.Error, Wrapper] = decode[Wrapper](data)
      val expected = Wrapper(IRI("http://example.com/visited"))
      decoded match {
        case Right(actual) => assert(actual === expected)
        case Left(_)       => false
      }
    }

    it("should throw a statement validation exception if the value is not a valid IRI") {
      val data: String = """{"id":"visited"}"""
      assertThrows[StatementValidationException] {
        decode[Wrapper](data)
      }
    }
  }

}
