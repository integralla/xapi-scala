package io.integralla.model.xapi.identifiers

import io.circe.generic.auto._
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.exceptions.StatementValidationException
import io.integralla.testing.spec.UnitSpec

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

  describe("[equivalence]") {
    it("should return true if both IRIs match") {
      val left = IRI("https://lrs.integralla.io/path/resource?a=1&b=2")
      val right = IRI("https://lrs.integralla.io/path/resource?a=1&b=2")
      assert(left.isEquivalentTo(right))
    }
    it("should return true if both IRIs match except for the schema case") {
      val left = IRI("https://lrs.integralla.io/path/resource?a=1&b=2")
      val right = IRI("HTTPS://lrs.integralla.io/path/resource?a=1&b=2")
      assert(left.isEquivalentTo(right))
    }
    it("should return true if both IRIs match except for the host case") {
      val left = IRI("https://lrs.integralla.io/path/resource?a=1&b=2")
      val right = IRI("https://LRS.INTEGRALLA.IO/path/resource?a=1&b=2")
      assert(left.isEquivalentTo(right))
    }
    it("should return true if both IRIs match except for ordering of query string parameters") {
      val left = IRI("https://lrs.integralla.io/path/resource?a=1&b=2")
      val right = IRI("https://lrs.integralla.io/path/resource?b=2&a=1")
      assert(left.isEquivalentTo(right))
    }
    it("should return true if both IRIs match except for percent encoding") {
      val left = IRI("https://example.org/~user")
      val right = IRI("https://example.org/%7euser")
      assert(left.isEquivalentTo(right))
    }
    it("should return true if both IRIs match except for percent encoding (case)") {
      val left = IRI("https://example.org/~user")
      val right = IRI("https://example.org/%7Euser")
      assert(left.isEquivalentTo(right))
    }
    it("should return true if both IRIs match with international characters") {
      val left = IRI("https://example.com/فعل/خواندن")
      val right = IRI("https://example.com/فعل/خواندن")
      assert(left.isEquivalentTo(right))
    }
    it("should return false if both IRIs do not match (domain)") {
      val left = IRI("https://lrs.integralla.io/path/resource?a=1&b=2")
      val right = IRI("https://lrs.integralla.com/path/resource?a=1&b=2")
      assert(left.isEquivalentTo(right) === false)
    }
    it("should return false if both IRIs do not match (path case)") {
      val left = IRI("https://lrs.integralla.io/path/resource?a=1&b=2")
      val right = IRI("https://lrs.integralla.io/PATH/resource?a=1&b=2")
      assert(left.isEquivalentTo(right) === false)
    }
    it("should return true if two URNs are the same") {
      val left = IRI("example:verb:visiterò")
      val right = IRI("example:verb:visiterò")
      assert(left.isEquivalentTo(right))
    }
    it("should return true if two URNs are the same except for schema case") {
      val left = IRI("example:verb:visiterò")
      val right = IRI("EXAMPLE:verb:visiterò")
      assert(left.isEquivalentTo(right))
    }
    it("should return false if two URNs are not the same") {
      val left = IRI("example:verb:visiterò")
      val right = IRI("example:verb:visited")
      assert(left.isEquivalentTo(right) === false)
    }
  }
}
