package io.integralla.model.xapi.statement.identifiers

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.testing.spec.UnitSpec

class AccountTest extends UnitSpec {

  describe("An Account") {

    // DECODING / ENCODING

    it("should support decoding from JSON") {
      val data = """{"homePage": "http://www.example.com", "name": "123456"}""".stripMargin
      val decoded: Either[io.circe.Error, Account] = decode[Account](data)
      val expected = Account(homePage = "http://www.example.com", name = "123456")
      decoded match {
        case Right(actual) => assert(actual === expected)
        case Left(_) => false
      }
    }

    it("should fail on decoding JSON missing the homePage property") {
      val data = """{"name": "123456"}""".stripMargin
      val decoded: Either[io.circe.Error, Account] = decode[Account](data)
      val success = decoded match {
        case Right(_) => true
        case Left(_) => false
      }
      assert(success === false)
    }

    it("should fail on decoding JSON missing the name property") {
      val data = """{"homePage": "http://www.example.com"}""".stripMargin
      val decoded: Either[io.circe.Error, Account] = decode[Account](data)
      val success = decoded match {
        case Right(_) => true
        case Left(_) => false
      }
      assert(success === false)
    }

    it("should fail on decoding JSON where the value of homePage is the wrong type") {
      val data = """{"homePage": null, "name": "123456"}""".stripMargin
      val decoded: Either[io.circe.Error, Account] = decode[Account](data)
      val success = decoded match {
        case Right(_) => true
        case Left(_) => false
      }
      assert(success === false)
    }

    it("should fail on decoding JSON where the value of name is the wrong type") {
      val data = """{"homePage": "http://www.example.com", "name": 123456}""".stripMargin
      val decoded: Either[io.circe.Error, Account] = decode[Account](data)
      val success = decoded match {
        case Right(_) => true
        case Left(_) => false
      }
      assert(success === false)
    }

    it("should support encoding as JSON") {
      val account: Account = Account(homePage = "http://www.example.com", name = "123456")
      val actual: String = account.asJson.noSpaces
      val expected = """{"homePage":"http://www.example.com","name":"123456"}""".stripMargin
      assert(actual === expected)
    }

    // VALIDATION

    it("should throw a validation exception if the homePage is not a valid IRI") {
      val data = """{"homePage": "www.example.com", "name": "123456"}""".stripMargin
      assertThrows[StatementValidationException] {
        decode[Account](data)
      }
    }

  }

}
