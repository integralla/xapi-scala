package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.IRI
import io.integralla.testing.spec.UnitSpec

class StatementVerbTest extends UnitSpec {

  describe("A Verb") {
    describe("[encoding]") {
      it("should successfully encode a verb with only an identifier") {
        val verb: StatementVerb = StatementVerb(IRI("http://example.com/visited"), None)
        val encoded: String = verb.asJson.noSpaces
        val expected: String = """{"id":"http://example.com/visited"}"""
        assert(encoded === expected)
      }

      it("should successfully encode a verb that includes a display language map") {
        val languageMap: LanguageMap = Map("en-US" -> "will visit", "it-IT" -> "visiterò")
        val verb: StatementVerb = StatementVerb(IRI("http://example.com/visited"), Some(languageMap))
        val encoded: String = verb.asJson.noSpaces
        val expected: String = """{"id":"http://example.com/visited","display":{"en-US":"will visit","it-IT":"visiterò"}}"""
        assert(encoded === expected)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a verb with only an identifier") {
        val data: String = """{"id":"http://example.com/visited"}"""
        val decoded: Either[io.circe.Error, StatementVerb] = decode[StatementVerb](data)
        val expected: StatementVerb = StatementVerb(IRI("http://example.com/visited"), None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a verb that includes a display language map") {
        val data: String = """{"id":"http://example.com/visited","display":{"en-US":"will visit","it-IT":"visiterò"}}"""
        val decoded: Either[io.circe.Error, StatementVerb] = decode[StatementVerb](data)
        val languageMap: LanguageMap = Map("en-US" -> "will visit", "it-IT" -> "visiterò")
        val expected: StatementVerb = StatementVerb(IRI("http://example.com/visited"), Some(languageMap))
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a verb where the identifier IRI includes international characters") {
        val data: String = """{"id":"http://example.com/فعل/خواندن"}"""
        val decoded: Either[io.circe.Error, StatementVerb] = decode[StatementVerb](data)
        val expected: StatementVerb = StatementVerb(IRI("http://example.com/فعل/خواندن"), None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should throw a statement validation error if the verb identifier is not a valid IRI") {
        val data: String = """{"id":"//example.com/visited","display":{"en-US":"will visit","it-IT":"visiterò"}}"""
        val exception = intercept[StatementValidationException] {
          decode[StatementVerb](data)
        }
        assert(exception.getMessage.contains("An IRI must be a valid URI, with a schema"))
      }
    }
  }

}
