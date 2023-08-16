package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.references.*
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import java.util.UUID
import scala.io.Source
import scala.util.Using

class StatementObjectTest extends UnitSpec {

  val nameLanguageMap: LanguageMap = LanguageMap(Map("en-US" -> "Example Activity", "it-IT" -> "Esempio di attività"))
  val descriptionLanguageMap: LanguageMap = LanguageMap(
    Map("en-US" -> "An xAPI activity", "it-IT" -> "Un'attività xAPI")
  )

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
    StatementVerb(IRI("http://example.com/visited"), Some(LanguageMap(Map("en-US" -> "will visit")))),
    StatementObject(
      Activity(
        Some(StatementObjectType.Activity),
        IRI("http://example.com/website"),
        Some(
          ActivityDefinition(
            Some(LanguageMap(Map("en-US" -> "Some Awesome Website"))),
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

      it("should successfully decode a statement object that is a sub-statement where the object type is not defined") {
        val encoded: String = """
            |{
            |  "objectType" : "SubStatement",
            |  "actor" : {
            |    "objectType" : "Agent",
            |    "mbox" : "mailto:test@example.com"
            |  },
            |  "verb" : {
            |    "id" : "http://example.com/visited",
            |    "display" : {
            |      "en-US" : "will visit"
            |    }
            |  },
            |  "object" : {
            |    "id" : "http://example.com/website",
            |    "definition" : {
            |      "name" : {
            |        "en-US" : "Some Awesome Website"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin
        val decoded: Either[io.circe.Error, StatementObject] = decode[StatementObject](encoded)
        decoded match {
          case Right(statementObject) =>
            assert(statementObject.value.isInstanceOf[SubStatement])
            assert(statementObject.value.asInstanceOf[SubStatement].`object`.value.isInstanceOf[Activity])
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }
    }

    describe("[equivalence]") {
      it("should return true if both objects are equivalent references") {
        val left: StatementObject = StatementObject(sampleActivity.copy())
        val right: StatementObject = StatementObject(sampleActivity.copy())
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both objects are equivalent agents") {
        val left: StatementObject = StatementObject(sampleAgent.copy())
        val right: StatementObject = StatementObject(sampleAgent.copy())
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both objects are equivalent groups") {
        val left: StatementObject = StatementObject(sampleGroup.copy())
        val right: StatementObject = StatementObject(sampleGroup.copy())
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both objects are equivalent statement references") {
        val left: StatementObject = StatementObject(sampleStatementRef.copy())
        val right: StatementObject = StatementObject(sampleStatementRef.copy())
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both objects are equivalent sub-statements") {
        val left: StatementObject = StatementObject(sampleSubStatement.copy())
        val right: StatementObject = StatementObject(sampleSubStatement.copy())
        assert(left.isEquivalentTo(right))
      }

      it("should return false if the objects are not equivalent") {
        val left: StatementObject = StatementObject(sampleStatementRef.copy())
        val right: StatementObject = StatementObject(sampleStatementRef.copy(id = UUID.randomUUID()))
        assert(left.isEquivalentTo(right) === false)
      }

      it("should return false if the objects are of a different type") {
        val left: StatementObject = StatementObject(sampleActivity.copy())
        val right: StatementObject = StatementObject(sampleStatementRef.copy())
        assert(left.isEquivalentTo(right) === false)
      }
    }

    describe("getActivityReferences") {
      it("should return a list with a single activity if the statement object is an activity") {
        val statementObject: StatementObject = StatementObject(sampleActivity.copy())
        val references: List[ActivityReference] = statementObject.getActivityReferences()
        assert(references.length === 1)
        assert(references.head.referenceType === ActivityObjectRef)
        assert(references.head.inSubStatement === false)
      }

      it("should return a non-empty list of the statement object is a sub-statement where the object is an activity") {
        val statementObject: StatementObject = StatementObject(sampleSubStatement.copy())
        val references: List[ActivityReference] = statementObject.getActivityReferences(true)
        assert(references.length === 1)
        assert(references.head.referenceType === ActivityObjectRef)
        assert(references.head.inSubStatement === true)
      }
      it("should return an empty list if the statement object is not an activity nor a sub-statement") {
        val statementObject: StatementObject = StatementObject(sampleAgent.copy())
        val references: List[ActivityReference] = statementObject.getActivityReferences()
        assert(references.isEmpty)
      }
    }

    describe("getAgentReferences") {
      it("should return a list composed of a single agent reference when the statement object is an agent") {
        val statementObject: StatementObject = StatementObject(sampleAgent.copy())
        val references: List[AgentReference] = statementObject.getAgentReferences(inSubStatement = false)
        assert(references.length === 1)
        assert(references.head.referenceType === AgentObjectRef)
        assert(references.head.inSubStatement === false)
        assert(references.head.asGroupMember === false)
      }

      it("should return a list of agent references when the statement object is a group") {
        val statementObject: StatementObject = StatementObject(sampleGroup.copy())
        val references: List[AgentReference] = statementObject.getAgentReferences(inSubStatement = false)
        assert(references.length === 1)
        assert(references.head.referenceType === AgentObjectRef)
        assert(references.head.inSubStatement === false)
        assert(references.head.asGroupMember === false)
      }

      it("should return a list of agent reference when the statement object is a sub-statement") {
        val statementObject: StatementObject = StatementObject(sampleSubStatement.copy())
        val references: List[AgentReference] = statementObject.getAgentReferences(inSubStatement = false)
        assert(references.length === 1)
        assert(references.head.referenceType === ActorRef)
        assert(references.head.inSubStatement === true)
        assert(references.head.asGroupMember === false)
      }

      it("should return an empty list when the statement object is an activity") {
        val statementObject: StatementObject = StatementObject(sampleActivity.copy())
        assert(statementObject.getAgentReferences(inSubStatement = false).isEmpty)
      }

      it("should return an empty list when the statement object is a statement-ref") {
        val statementObject: StatementObject = StatementObject(sampleStatementRef.copy())
        assert(statementObject.getAgentReferences(inSubStatement = false).isEmpty)
      }
    }
  }
}
