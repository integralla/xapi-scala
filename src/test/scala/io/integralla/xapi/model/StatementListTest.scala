package io.integralla.xapi.model

import io.integralla.xapi.model.exceptions.StatementValidationException
import org.scalatest.funspec.AnyFunSpec

import java.util.UUID
import scala.util.Try

class StatementListTest extends AnyFunSpec {

  def generateStatement: Statement = Statement(
    id = Some(UUID.randomUUID()),
    actor = Agent(
      objectType = Some(StatementObjectType.Agent),
      mbox = Some(MBox("mailto:xapi@adlnet.gov"))
    ),
    verb = StatementVerb(
      IRI("http://adlnet.gov/expapi/verbs/created"),
      Some(LanguageMap(Map("en-US" -> "created")))
    ),
    `object` = StatementObject(
      Activity(None, IRI("http://example.adlnet.gov/xapi/example/activity"), None)
    )
  )

  describe("StatementList") {
    describe("[encoding/decoding]") {
      it("should encode/decode a list of statements") {
        val statements: List[Statement] = (1 to 2).toList.map(_ => generateStatement)
        val statementList: StatementList = new StatementList(statements)
        val encoded: String = statementList.toJson()
        assert(encoded.startsWith("""[{"id":"""))

        val decoded: Try[StatementList] = StatementList(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === statementList)
      }
    }

    describe("[validation]") {
      it(
        "should throw a statement validation exception if any statement in the list does not validate"
      ) {
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
          StatementList(statements).get
        }
        assert(exception.getMessage.contains("An Agent mbox identifier must be a valid mailto IRI"))
      }
    }
  }
}
