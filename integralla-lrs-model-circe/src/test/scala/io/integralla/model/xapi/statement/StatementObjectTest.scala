package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import java.util.UUID
import scala.io.Source
import scala.util.Using

class StatementObjectTest extends UnitSpec {

  val nameLanguageMap: LanguageMap = Map("en-US" -> "Example Activity", "it-IT" -> "Esempio di attività")
  val descriptionLanguageMap: LanguageMap = Map("en-US" -> "An xAPI activity", "it-IT" -> "Un'attività xAPI")

  val sampleActivityDefinition: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )
  val sampleActivity: Activity = Activity(
    Some(StatementObjectType.Activity),
    IRI("http://example.com/xapi/activity/simplestatement"),
    Some(sampleActivityDefinition)
  )
  val sampleActivityEncoded: String =
    """{"objectType":"Activity","id":"http://example.com/xapi/activity/simplestatement","definition":{"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"}}}"""

  val sampleAgent: Agent = Agent(
    Some(StatementObjectType.Agent),
    Some("John Doe"),
    Some(MBox("mailto:john.doe@example.com")),
    None,
    None,
    None
  )
  val sampleAgentEncoded: String = """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""

  val sampleGroup: Group =
    Group(StatementObjectType.Group, Some("Team A"), Some(MBox("mailto:team.a@example.com")), None, None, None, None)
  val sampleGroupEncoded: String = """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com"}"""

  val sampleStatementRef: StatementRef =
    StatementRef(StatementObjectType.StatementRef, UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2"))
  val sampleStatementRefEncoded: String =
    """{"objectType":"StatementRef","id":"7cf5941a-9631-4741-83eb-28beb8ff28e2"}"""

  val sampleSubStatement: SubStatement = SubStatement(
    StatementObjectType.SubStatement,
    Agent(Some(StatementObjectType.Agent), None, Some(MBox("mailto:test@example.com")), None, None, None),
    StatementVerb(IRI("http://example.com/visited"), Some(Map("en-US" -> "will visit"))),
    StatementObject(
      Activity(
        Some(StatementObjectType.Activity),
        IRI("http://example.com/website"),
        Some(
          ActivityDefinition(
            Some(Map("en-US" -> "Some Awesome Website")),
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None
          )
        )
      )
    ),
    None,
    None,
    None,
    None
  )
  val sampleSubStatementEncoded: String =
    Using.resource(Source.fromResource("data/sample-sub-statement.json"))(_.mkString)

  describe("StatementObject") {
    describe("[encoding]") {
      it("should successfully encode a statement object that is an activity") {
        val statementObject: StatementObject = StatementObject(sampleActivity)
        val actual = statementObject.asJson.noSpaces
        assert(actual === sampleActivityEncoded)
      }

      it("should successfully encode a statement object that is an agent") {
        val statementObject: StatementObject = StatementObject(sampleAgent)
        val actual = statementObject.asJson.noSpaces
        assert(actual === sampleAgentEncoded)
      }

      it("should successfully encode a statement object that is an group") {
        val statementObject: StatementObject = StatementObject(sampleGroup)
        val actual = statementObject.asJson.noSpaces
        assert(actual === sampleGroupEncoded)
      }

      it("should successfully encode a statement object that is a statement reference") {
        val statementObject: StatementObject = StatementObject(sampleStatementRef)
        val actual = statementObject.asJson.noSpaces
        assert(actual === sampleStatementRefEncoded)
      }

      it("should successfully encode a statement object that is a sub-statement") {
        val statementObject: StatementObject = StatementObject(sampleSubStatement)
        val actual = statementObject.asJson.spaces2
        assert(actual === sampleSubStatementEncoded)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a statement object that is an activity") {
        val decoded: Either[io.circe.Error, StatementObject] = decode[StatementObject](sampleActivityEncoded)
        val expected: StatementObject = StatementObject(sampleActivity)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement object that is an activity without an explicit object type") {
        val data: String = """{"id":"http://example.com/xapi/activity/simplestatement"}"""
        val decoded: Either[io.circe.Error, StatementObject] = decode[StatementObject](data)
        val expected: StatementObject =
          StatementObject(Activity(None, IRI("http://example.com/xapi/activity/simplestatement"), None))
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement object that is an activity without a definition") {
        val data: String = """{"objectType":"Activity","id":"http://example.com/xapi/activity/simplestatement"}"""
        val decoded: Either[io.circe.Error, StatementObject] = decode[StatementObject](data)
        val expected: StatementObject = StatementObject(
          Activity(Some(StatementObjectType.Activity), IRI("http://example.com/xapi/activity/simplestatement"), None)
        )
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement object that is an agent") {
        val decoded: Either[io.circe.Error, StatementObject] = decode[StatementObject](sampleAgentEncoded)
        val expected: StatementObject = StatementObject(sampleAgent)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it(
        "should throw a statement validation exception if the statement object is an agent where the objectType is not explicitly set"
      ) {
        val agentEncoded: String = """{"name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
        val exception = intercept[StatementValidationException] {
          decode[StatementObject](agentEncoded)
        }
        assert(
          exception.getMessage.startsWith(
            "An objectType must be explicitly set for any object type other than Activity"
          )
        )
      }

      it(
        "should throw a statement validation exception if the statement object is an statement ref where the objectType is not explicitly set"
      ) {
        val statementRefEncoded: String = """{"id": "2fcbb5ea-4b75-44d5-bf51-e8c2d690d658"}"""
        val exception = intercept[StatementValidationException] {
          decode[StatementObject](statementRefEncoded)
        }
        assert(
          exception.getMessage.startsWith(
            "An objectType must be explicitly set for any object type other than Activity"
          )
        )
      }

      it("should successfully decode a statement object that is a group") {
        val decoded: Either[io.circe.Error, StatementObject] = decode[StatementObject](sampleGroupEncoded)
        val expected: StatementObject = StatementObject(sampleGroup)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement object that is a statement reference") {
        val decoded: Either[io.circe.Error, StatementObject] = decode[StatementObject](sampleStatementRefEncoded)
        val expected: StatementObject = StatementObject(sampleStatementRef)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement object that is a sub-statement") {
        val decoded: Either[io.circe.Error, StatementObject] = decode[StatementObject](sampleSubStatementEncoded)
        val expected: StatementObject = StatementObject(sampleSubStatement)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }
    }
  }
}
