package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{Account, IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import java.time.OffsetDateTime
import java.util.UUID
import scala.io.Source
import scala.util.Using

class StatementTest extends UnitSpec {

  val basicStatement: Statement = Statement(
    Some(UUID.fromString("12345678-1234-5678-1234-567812345678")),
    Agent(Some(StatementObjectType.Agent), None, Some(MBox("mailto:xapi@adlnet.gov")), None, None, None),
    StatementVerb(IRI("http://adlnet.gov/expapi/verbs/created"), Some(Map("en-US" -> "created"))),
    StatementObject(
      Activity(None, IRI("http://example.adlnet.gov/xapi/example/activity"), None)
    ),
    None, None, None, None, None, None, None
  )

  val sampleAgentActor: StatementActor = new Agent(Some(StatementObjectType.Agent), Some("John Doe"), Some(MBox("mailto:john.doe@example.com")), None, None, None)
  val sampleGroupActor: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), Some(MBox("mailto:team.a@example.com")), None, None, None, Some(List(
    new Agent(Some(StatementObjectType.Agent), Some("John Doe"), Some(MBox("mailto:john.doe@example.com")), None, None, None),
    new Agent(Some(StatementObjectType.Agent), Some("Richard Roe"), Some(MBox("mailto:richard.roe@example.com")), None, None, None)
  )))

  val sampleVerb: StatementVerb = StatementVerb(IRI("http://example.com/visited"), Some(Map("en-US" -> "will visit", "it-IT" -> "visiterò")))

  val sampleActivityObject: StatementObject = StatementObject(
    Activity(
      Some(StatementObjectType.Activity),
      IRI("http://example.com/xapi/activity/simplestatement"),
      Some(
        ActivityDefinition(
          Some(Map("en-US" -> "Example Activity", "it-IT" -> "Esempio di attività")),
          Some(Map("en-US" -> "An xAPI activity", "it-IT" -> "Un'attività xAPI")),
          None, None, None, None, None, None, None, None, None, None
        )
      )
    )
  )

  val sampleInteractionActivityObject: StatementObject = StatementObject(
    Activity(
      Some(StatementObjectType.Activity),
      IRI("http://example.adlnet.gov/xapi/example/activity"),
      Some(
        ActivityDefinition(
          Some(Map("en-US" -> "Example Activity")),
          Some(Map("en-US" -> "An xAPI example activity")),
          Some(IRI("http://adlnet.gov/expapi/activities/cmi.interaction")),
          Some(IRI("https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C")),
          Some(InteractionType.CHOICE),
          Some(List("golf[,]tetris")),
          Some(
            List(
              InteractionComponent("golf", Some(Map("en-US" -> "Golf Example"))),
              InteractionComponent("facebook", Some(Map("en-US" -> "Facebook App"))),
              InteractionComponent("tetris", Some(Map("en-US" -> "Tetris Example"))),
              InteractionComponent("scrabble", Some(Map("en-US" -> "Scrabble Example")))
            )
          ),
          None, None, None, None, None)
      )
    )
  )

  val sampleAgentObject: StatementObject = StatementObject(
    Agent(Some(StatementObjectType.Agent), Some("Andrew Downes"), Some(MBox("mailto:andrew@example.co.uk")), None, None, None)
  )

  val sampleGroupObject: StatementObject = StatementObject(
    Group(
      StatementObjectType.Group, Some("Example Group"), None, None, None,
      Some(Account("http://example.com/homePage", "GroupAccount")),
      Some(
        List(
          Agent(Some(StatementObjectType.Agent), Some("Andrew Downes"), Some(MBox("mailto:andrew@example.co.uk")), None, None, None),
          Agent(Some(StatementObjectType.Agent), Some("Aaron Silvers"), None, None, Some("http://aaron.openid.example.org"), None)
        )
      )
    )
  )

  val sampleStatementRef: StatementRef = StatementRef(StatementObjectType.StatementRef, UUID.fromString("f1dc3573-e346-4bd0-b295-f5dde5cbe13f"))
  val sampleStatementRefObject: StatementObject = StatementObject(sampleStatementRef)

  val sampleSubStatementObject: StatementObject = StatementObject(
    SubStatement(
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
              None, None, None, None, None, None, None, None, None, None, None
            )
          )
        )
      ),
      None, None, None, None
    )
  )

  val sampleResult: StatementResult = StatementResult(
    Some(Score(Some(0.5), Some(5.0), Some(0.0), Some(10.0))),
    Some(true), Some(true), Some("response"), Some("PT4H35M59.14S"),
    None
  )

  val sampleContext: StatementContext = StatementContext(
    None, None, None,
    Some(
      ContextActivities(
        Some(List(Activity(Some(StatementObjectType.Activity), IRI("http://www.example.com/meetings/series/267"), None))),
        None, None, None)),
    None, None, None, None, None
  )

  val sampleOffsetDateTime: OffsetDateTime = OffsetDateTime.parse("2021-09-17T10:48:38-04:00")

  val sampleAttachment: Attachment = Attachment(
    IRI("http://adlnet.gov/expapi/attachments/signature"),
    Map("en-US" -> "Signature"),
    Some(Map("en-US" -> "A test signature")),
    "application/octet-stream",
    4235,
    "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634",
    None
  )


  describe("Statement") {
    describe("[encoding]") {
      it("should encode a simple statement") {
        val actual: String = basicStatement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-simplest.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement where the actor is an agent") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, None, None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-actor-is-agent.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement where the actor is an group") {
        val statement = Statement(None, sampleGroupActor, sampleVerb, sampleActivityObject, None, None, None, None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-actor-is-group.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is an activity") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleInteractionActivityObject, None, None, None, None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-object-is-choice-activity.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is an agent") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleAgentObject, None, None, None, None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-object-is-agent.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is a group") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleGroupObject, None, None, None, None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-object-is-group.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is a statement reference") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleStatementRefObject, None, None, None, None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-object-is-statement-ref.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is a sub-statement") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleSubStatementObject, None, None, None, None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-object-is-sub-statement.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement with a result object") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, Some(sampleResult), None, None, None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-with-result.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement with a context object") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, Some(sampleContext), None, None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-with-context.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement with a timestamp property") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, Some(sampleOffsetDateTime), None, None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-with-timestamp.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement with a stored property") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, Some(sampleOffsetDateTime), Some(sampleOffsetDateTime), None, None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-with-stored.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement with an authority property") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, None, None, Some(sampleAgentActor), None, None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-with-authority.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement with a version property") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, None, None, None, Some("1.0.0"), None)
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-with-version.json"))(_.mkString)
        assert(actual === expected)
      }

      it("should successfully encode a statement with an attachments property") {
        val statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, None, None, None, None, Some(List(sampleAttachment)))
        val actual = statement.asJson.spaces2
        val expected: String = Using.resource(Source.fromResource("data/sample-statement-with-attachments.json"))(_.mkString)
        assert(actual === expected)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a simple statement") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-simplest.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        decoded match {
          case Right(actual) => assert(actual === basicStatement)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement where the actor is an agent") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-actor-is-agent.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, None, None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement where the actor is an group") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-actor-is-group.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleGroupActor, sampleVerb, sampleActivityObject, None, None, None, None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement where the object is an activity object") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-object-is-choice-activity.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleInteractionActivityObject, None, None, None, None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement where the object is an agent") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-object-is-agent.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleAgentObject, None, None, None, None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement where the object is a group") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-object-is-group.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleGroupObject, None, None, None, None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement where the object is a statement reference") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-object-is-statement-ref.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleStatementRefObject, None, None, None, None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement where the object is a sub-statement") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-object-is-sub-statement.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleSubStatementObject, None, None, None, None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement with a result object") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-with-result.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, Some(sampleResult), None, None, None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement with a context object") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-with-context.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, Some(sampleContext), None, None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement with a timestamp property") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-with-timestamp.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, Some(sampleOffsetDateTime), None, None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement with a stored property") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-with-stored.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, Some(sampleOffsetDateTime), Some(sampleOffsetDateTime), None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement with an authority property") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-with-authority.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, None, None, Some(sampleAgentActor), None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement with a version property") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-with-version.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, None, None, None, Some("1.0.0"), None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement with an attachments property") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-with-attachments.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        val expected: Statement = Statement(None, sampleAgentActor, sampleVerb, sampleActivityObject, None, None, None, None, None, None, Some(List(sampleAttachment)))
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a complex statement that showcases most statement properties") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-property-showcase.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        decoded match {
          case Right(actual) =>
            assert(actual.id === Some(UUID.fromString("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")))
            actual.actor match {
              case group: Group => assert(group.member.isDefined)
              case _ => ()
            }
            assert(actual.result.isDefined)
            assert(actual.context.isDefined)
            assert(actual.context.get.contextActivities.isDefined)
            assert(actual.stored.isDefined)
            assert(actual.authority.isDefined)
            assert(actual.version.isDefined)
            assert(actual.`object`.value.isInstanceOf[Activity])
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a statement following the cmi5 Community of Practice") {
        val data: String = Using.resource(Source.fromResource("data/sample-statement-cmi5-example.json"))(_.mkString)
        val decoded: Either[io.circe.Error, Statement] = decode[Statement](data)
        decoded match {
          case Right(actual) =>
            assert(actual.id === Some(UUID.fromString("2a41c918-b88b-4220-20a5-a4c32391a240")))
            assert(actual.result.isDefined)
            assert(actual.context.isDefined)
            assert(actual.context.get.contextActivities.isDefined)
            assert(actual.context.get.extensions.isDefined)
            assert(actual.timestamp === Some(OffsetDateTime.parse("2012-06-01T19:09:13.245+00:00")))
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }
    }

    describe("[validation]") {
      it("should throw a statement validation error if the context.revision property is set when the statements object is not an activity") {
        val exception = intercept[StatementValidationException] {
          Statement(
            Some(UUID.fromString("12345678-1234-5678-1234-567812345678")),
            Agent(Some(StatementObjectType.Agent), None, Some(MBox("mailto:xapi@adlnet.gov")), None, None, None),
            StatementVerb(IRI("http://adlnet.gov/expapi/verbs/created"), Some(Map("en-US" -> "created"))),
            StatementObject(
              StatementRef(StatementObjectType.StatementRef, UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2"))
            ), None,
            Some(StatementContext(None, None, None, None, Some("1.0.0"), None, None, None, None)),
            None, None, None, None, None
          )
        }
        assert(exception.getMessage.contains("""The "revision" property on the context object must only be used if the statement's object is an activity"""))
      }

      it("should throw a statement validation error if the context.platform property is set when the statements object is not an activity") {
        val exception = intercept[StatementValidationException] {
          Statement(
            Some(UUID.fromString("12345678-1234-5678-1234-567812345678")),
            Agent(Some(StatementObjectType.Agent), None, Some(MBox("mailto:xapi@adlnet.gov")), None, None, None),
            StatementVerb(IRI("http://adlnet.gov/expapi/verbs/created"), Some(Map("en-US" -> "created"))),
            StatementObject(
              StatementRef(StatementObjectType.StatementRef, UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2"))
            ), None,
            Some(StatementContext(None, None, None, None, None, Some("lrp"), None, None, None)),
            None, None, None, None, None
          )
        }
        assert(exception.getMessage.contains("""The "platform" property on the context object must only be used if the statement's object is an activity"""))
      }
    }
  }
}
