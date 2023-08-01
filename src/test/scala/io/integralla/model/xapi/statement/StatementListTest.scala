package io.integralla.model.xapi.statement

import io.circe
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import java.util.UUID

class StatementListTest extends UnitSpec {

  def generateStatement: Statement = Statement(
    Some(UUID.randomUUID()),
    Agent(Some(StatementObjectType.Agent), None, Some(MBox("mailto:xapi@adlnet.gov")), None, None, None),
    StatementVerb(IRI("http://adlnet.gov/expapi/verbs/created"), Some(LanguageMap(Map("en-US" -> "created")))),
    StatementObject(
      Activity(None, IRI("http://example.adlnet.gov/xapi/example/activity"), None)
    ),
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  describe("StatementList") {
    describe("[encoding/decoding]") {
      it("should encode/decode a list of statements") {
        val statements: List[Statement] = (1 to 2).toList.map(_ => generateStatement)
        val statementList: StatementList = new StatementList(statements)
        val encoded: String = statementList.asJson.noSpaces
        assert(encoded.startsWith("""[{"id":"""))

        val decoded: Either[circe.Error, StatementList] = decode[StatementList](encoded)
        decoded match {
          case Right(actual) => assert(actual === statementList)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }
    }

    describe("[validation]") {
      it("should throw a statement validation exception if any statement in the list does not validate") {
        val statements: String =
          """[
          |	{
          |		"actor": {
          |			"mbox": "xapi@adlnet.gov"
          |		},
          |		"verb": {
          |			"id": "http://adlnet.gov/expapi/verbs/created"
          |		},
          |		"object": {
          |			"id": "http://example.adlnet.gov/xapi/example/activity"
          |		}
          |	}
          |]
          |""".stripMargin

        val exception = intercept[StatementValidationException] {
          decode[StatementList](statements)
        }
        assert(exception.getMessage.contains("An Agent mbox identifier must be a valid mailto IRI"))
      }
    }
  }
}
