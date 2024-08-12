package io.integralla.xapi.model

import io.integralla.xapi.model.exceptions.StatementValidationException
import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

class AccountTest extends AnyFunSpec {

  describe("An Account") {
    describe("[validation]") {
      it("should throw a validation exception if the homePage is not a valid IRI") {
        val data = """{"homePage": "www.example.com", "name": "123456"}""".stripMargin
        assertThrows[StatementValidationException] {
          Account(data)
        }
      }
    }

    describe("[encoding]") {
      it("should support encoding as JSON") {
        val account: Account = Account(homePage = "http://www.example.com", name = "123456")
        val actual: String = account.toJson()
        val expected = """{"homePage":"http://www.example.com","name":"123456"}""".stripMargin
        assert(actual === expected)
      }
    }

    describe("[decoding]") {
      it("should support decoding from JSON") {
        val data = """{"homePage": "http://www.example.com", "name": "123456"}""".stripMargin
        val decoded: Try[Account] = Account(data)
        val expected = Account(homePage = "http://www.example.com", name = "123456")

        assert(decoded.isSuccess)
        assert(decoded.get === expected)

      }

      it("should fail on decoding JSON missing the homePage property") {
        val data = """{"name": "123456"}""".stripMargin
        val decoded: Try[Account] = Account(data)
        assert(decoded.isFailure)
      }

      it("should fail on decoding JSON missing the name property") {
        val data = """{"homePage": "http://www.example.com"}""".stripMargin
        val decoded: Try[Account] = Account(data)
        assert(decoded.isFailure)
      }

      it("should fail on decoding JSON where the value of homePage is the wrong type") {
        val data = """{"homePage": null, "name": "123456"}""".stripMargin
        val decoded: Try[Account] = Account(data)
        assert(decoded.isFailure)
      }

      it("should fail on decoding JSON where the value of name is the wrong type") {
        val data = """{"homePage": "http://www.example.com", "name": 123456}""".stripMargin
        val decoded: Try[Account] = Account(data)
        assert(decoded.isFailure)
      }
    }

    describe("[equivalence]") {
      it("should return true if both accounts match") {
        val left =
          Account(homePage = "https://lrs.integralla.io/accounts/", name = "populus.tremuloides")
        val right =
          Account(homePage = "https://lrs.integralla.io/accounts/", name = "populus.tremuloides")
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both accounts match except for the home page schema case") {
        val left =
          Account(homePage = "https://lrs.integralla.io/accounts/", name = "populus.tremuloides")
        val right =
          Account(homePage = "HTTPS://lrs.integralla.io/accounts/", name = "populus.tremuloides")
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both accounts match except for the home page host case") {
        val left =
          Account(homePage = "https://lrs.integralla.io/accounts/", name = "populus.tremuloides")
        val right =
          Account(homePage = "https://LRS.INTEGRALLA.IO/accounts/", name = "populus.tremuloides")
        assert(left.isEquivalentTo(right))
      }
      it("should return false if the accounts don't match (homepage)") {
        val left =
          Account(homePage = "https://lrs.integralla.io/accounts/", name = "populus.tremuloides")
        val right =
          Account(homePage = "https://lrs.integralla.com/accounts/", name = "populus.tremuloides")
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the accounts don't match (name case)") {
        val left =
          Account(homePage = "https://lrs.integralla.io/accounts/", name = "populus.tremuloides")
        val right =
          Account(homePage = "https://lrs.integralla.io/accounts/", name = "POPULUS.TREMULOIDES")
        assert(left.isEquivalentTo(right) === false)
      }
    }
  }
}
