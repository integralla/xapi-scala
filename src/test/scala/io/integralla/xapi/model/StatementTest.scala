package io.integralla.xapi.model

import com.typesafe.scalalogging.StrictLogging
import io.integralla.xapi.model.exceptions.StatementValidationException
import io.integralla.xapi.model.references._
import org.scalatest.funspec.AnyFunSpec

import java.time.{OffsetDateTime, ZoneId}
import java.util.UUID
import scala.io.Source
import scala.util.{Try, Using}

class StatementTest extends AnyFunSpec with StrictLogging {

  val basicStatement: Statement = Statement(
    id = Some(UUID.fromString("12345678-1234-5678-1234-567812345678")),
    actor = Agent(
      objectType = Some(StatementObjectType.Agent),
      mbox = Some(MBox("mailto:xapi@adlnet.gov"))
    ),
    verb = StatementVerb(
      id = IRI("http://adlnet.gov/expapi/verbs/created"),
      display = Some(LanguageMap(Map("en-US" -> "created")))
    ),
    `object` = StatementObject(
      Activity(None, IRI("http://example.adlnet.gov/xapi/example/activity"), None)
    )
  )

  val sampleAgentActor: StatementActor = new Agent(
    objectType = Some(StatementObjectType.Agent),
    name = Some("John Doe"),
    mbox = Some(MBox("mailto:john.doe@example.com"))
  )
  val sampleGroupActor: StatementActor = new Group(
    objectType = StatementObjectType.Group,
    name = Some("Team A"),
    mbox = Some(MBox("mailto:team.a@example.com")),
    member = Some(
      List(
        new Agent(
          Some(StatementObjectType.Agent),
          Some("John Doe"),
          Some(MBox("mailto:john.doe@example.com"))
        ),
        new Agent(
          Some(StatementObjectType.Agent),
          Some("Richard Roe"),
          Some(MBox("mailto:richard.roe@example.com"))
        )
      )
    )
  )

  val sampleVerb: StatementVerb =
    StatementVerb(
      IRI("http://example.com/visited"),
      Some(LanguageMap(Map("en-US" -> "will visit", "it-IT" -> "visiterò")))
    )

  val sampleActivityObject: StatementObject = StatementObject(
    Activity(
      Some(StatementObjectType.Activity),
      IRI("http://example.com/xapi/activity/simplestatement"),
      Some(
        ActivityDefinition(
          Some(LanguageMap(Map("en-US" -> "Example Activity", "it-IT" -> "Esempio di attività"))),
          Some(LanguageMap(Map("en-US" -> "An xAPI activity", "it-IT" -> "Un'attività xAPI")))
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
          Some(LanguageMap(Map("en-US" -> "Example Activity"))),
          Some(LanguageMap(Map("en-US" -> "An xAPI example activity"))),
          Some(IRI("http://adlnet.gov/expapi/activities/cmi.interaction")),
          Some(IRI("https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C")),
          Some(InteractionType.CHOICE),
          Some(CorrectResponsePattern(List("golf[,]tetris"))),
          Some(
            List(
              InteractionComponent("golf", Some(LanguageMap(Map("en-US" -> "Golf Example")))),
              InteractionComponent("facebook", Some(LanguageMap(Map("en-US" -> "Facebook App")))),
              InteractionComponent("tetris", Some(LanguageMap(Map("en-US" -> "Tetris Example")))),
              InteractionComponent(
                "scrabble",
                Some(LanguageMap(Map("en-US" -> "Scrabble Example")))
              )
            )
          )
        )
      )
    )
  )

  val sampleAgentObject: StatementObject = StatementObject(
    Agent(
      objectType = Some(StatementObjectType.Agent),
      name = Some("Andrew Downes"),
      mbox = Some(MBox("mailto:andrew@example.co.uk"))
    )
  )

  val sampleGroupObject: StatementObject = StatementObject(
    Group(
      objectType = StatementObjectType.Group,
      name = Some("Example Group"),
      account = Some(Account("http://example.com/homePage", "GroupAccount")),
      member = Some(
        List(
          Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("Andrew Downes"),
            mbox = Some(MBox("mailto:andrew@example.co.uk"))
          ),
          Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("Aaron Silvers"),
            openid = Some("http://aaron.openid.example.org")
          )
        )
      )
    )
  )

  val sampleStatementRef: StatementRef =
    StatementRef(
      StatementObjectType.StatementRef,
      UUID.fromString("f1dc3573-e346-4bd0-b295-f5dde5cbe13f")
    )
  val sampleStatementRefObject: StatementObject = StatementObject(sampleStatementRef)

  val sampleSubStatementObject: StatementObject = StatementObject(
    SubStatement(
      StatementObjectType.SubStatement,
      Agent(
        objectType = Some(StatementObjectType.Agent),
        mbox = Some(MBox("mailto:test@example.com"))
      ),
      StatementVerb(
        IRI("http://example.com/visited"),
        Some(LanguageMap(Map("en-US" -> "will visit")))
      ),
      StatementObject(
        Activity(
          Some(StatementObjectType.Activity),
          IRI("http://example.com/website"),
          Some(
            ActivityDefinition(
              Some(LanguageMap(Map("en-US" -> "Some Awesome Website")))
            )
          )
        )
      )
    )
  )

  val sampleResult: StatementResult = StatementResult(
    Some(Score(Some(0.5), Some(5.0), Some(0.0), Some(10.0))),
    Some(true),
    Some(true),
    Some("response"),
    Some("PT4H35M59.14S"),
    None
  )

  val sampleContext: StatementContext = StatementContext(
    contextActivities = Some(
      ContextActivities(
        Some(
          List(
            Activity(
              Some(StatementObjectType.Activity),
              IRI("http://www.example.com/meetings/series/267")
            )
          )
        )
      )
    )
  )

  val sampleOffsetDateTime: OffsetDateTime = OffsetDateTime.parse("2021-09-17T10:48:38-04:00")

  val sampleAttachment: Attachment = Attachment(
    IRI("http://adlnet.gov/expapi/attachments/signature"),
    LanguageMap(Map("en-US" -> "Signature")),
    Some(LanguageMap(Map("en-US" -> "A test signature"))),
    "application/octet-stream",
    4235,
    "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634",
    None
  )

  def getStatementResource(path: String): String = {
    Using.resource(Source.fromResource(path))(_.mkString)
  }

  def getStatementFromResource(path: String): Statement = {
    Statement(getStatementResource(path)).get
  }

  describe("Statement") {
    describe("[encoding]") {
      it("should encode a simple statement") {
        val actual: String = basicStatement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-simplest.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement where the actor is an agent") {
        val statement =
          Statement(
            actor = sampleAgentActor,
            verb = sampleVerb,
            `object` = sampleActivityObject
          )
        val actual = statement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-actor-is-agent.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement where the actor is an group") {
        val statement =
          Statement(
            actor = sampleGroupActor,
            verb = sampleVerb,
            `object` = sampleActivityObject
          )
        val actual = statement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-actor-is-group.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is an activity") {
        val statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleInteractionActivityObject
        )
        val actual = statement.toJson(spaces = true)
        val expected: String =
          getStatementResource("data/sample-statement-object-is-choice-activity.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is an agent") {
        val statement =
          Statement(
            actor = sampleAgentActor,
            verb = sampleVerb,
            `object` = sampleAgentObject
          )
        val actual = statement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-object-is-agent.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is a group") {
        val statement =
          Statement(
            actor = sampleAgentActor,
            verb = sampleVerb,
            `object` = sampleGroupObject
          )
        val actual = statement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-object-is-group.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is a statement reference") {
        val statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleStatementRefObject
        )
        val actual = statement.toJson(spaces = true)
        val expected: String =
          getStatementResource("data/sample-statement-object-is-statement-ref.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement where the object is a sub-statement") {
        val statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleSubStatementObject
        )
        val actual = statement.toJson(spaces = true)
        val expected: String =
          getStatementResource("data/sample-statement-object-is-sub-statement.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement with a result object") {
        val statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          result = Some(sampleResult)
        )
        val actual = statement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-with-result.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement with a context object") {
        val statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          context = Some(sampleContext)
        )
        val actual = statement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-with-context.json")
        assert(actual === expected)
      }

      it(
        "should successfully encode a statement with a timestamp property, preserving millisecond precision"
      ) {
        val timestamp: String = "2023-08-21T16:55:59.000Z"
        val statement: Statement = Statement(
          actor = Agent(mbox = Some(MBox("mailto:lorum.ipsum@integralla.io"))),
          verb = StatementVerb(IRI("https://lrs.integralla.io/xapi/verbs/test")),
          `object` =
            StatementObject(Activity(id = IRI("https://lrs.integralla.io/xapi/activity/test"))),
          timestamp = Some(OffsetDateTime.parse(timestamp))
        )

        val expected: String =
          """{
            |  "actor" : {
            |    "mbox" : "mailto:lorum.ipsum@integralla.io"
            |  },
            |  "verb" : {
            |    "id" : "https://lrs.integralla.io/xapi/verbs/test"
            |  },
            |  "object" : {
            |    "id" : "https://lrs.integralla.io/xapi/activity/test"
            |  },
            |  "timestamp" : "2023-08-21T16:55:59.000000000Z"
            |}""".stripMargin

        val encoded: String = statement.toJson(spaces = true)
        assert(encoded === expected)
      }

      it("should successfully encode a statement with a stored property") {
        val timestamp: String = "2023-08-21T16:55:59.000Z"
        val statement: Statement = Statement(
          actor = Agent(mbox = Some(MBox("mailto:lorum.ipsum@integralla.io"))),
          verb = StatementVerb(IRI("https://lrs.integralla.io/xapi/verbs/test")),
          `object` =
            StatementObject(Activity(id = IRI("https://lrs.integralla.io/xapi/activity/test"))),
          stored = Some(OffsetDateTime.parse(timestamp))
        )

        val expected: String =
          """{
            |  "actor" : {
            |    "mbox" : "mailto:lorum.ipsum@integralla.io"
            |  },
            |  "verb" : {
            |    "id" : "https://lrs.integralla.io/xapi/verbs/test"
            |  },
            |  "object" : {
            |    "id" : "https://lrs.integralla.io/xapi/activity/test"
            |  },
            |  "stored" : "2023-08-21T16:55:59.000000000Z"
            |}""".stripMargin

        val encoded: String = statement.toJson(spaces = true)
        assert(encoded === expected)
      }

      it("should successfully encode a statement with an authority property") {
        val statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          authority = Some(sampleAgentActor)
        )
        val actual = statement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-with-authority.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement with a version property") {
        val statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          version = Some(XApiVersion(1, 0, Some(0)))
        )
        val actual = statement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-with-version.json")
        assert(actual === expected)
      }

      it("should successfully encode a statement with an attachments property") {
        val statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          attachments = Some(List(sampleAttachment))
        )
        val actual = statement.toJson(spaces = true)
        val expected: String = getStatementResource("data/sample-statement-with-attachments.json")
        assert(actual === expected)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a simple statement") {
        val data: String = getStatementResource("data/sample-statement-simplest.json")
        val decoded: Try[Statement] = Statement(data)
        assert(decoded.isSuccess)
        assert(decoded.get === basicStatement)
      }

      it("should successfully decode a statement where the actor is an agent") {
        val data: String = getStatementResource("data/sample-statement-actor-is-agent.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement =
          Statement(
            actor = sampleAgentActor,
            verb = sampleVerb,
            `object` = sampleActivityObject
          )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement where the actor is an group") {
        val data: String = getStatementResource("data/sample-statement-actor-is-group.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement =
          Statement(
            actor = sampleGroupActor,
            verb = sampleVerb,
            `object` = sampleActivityObject
          )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement where the object is an activity object") {
        val data: String =
          getStatementResource("data/sample-statement-object-is-choice-activity.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleInteractionActivityObject
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement where the object is an agent") {
        val data: String = getStatementResource("data/sample-statement-object-is-agent.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement =
          Statement(
            actor = sampleAgentActor,
            verb = sampleVerb,
            `object` = sampleAgentObject
          )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement where the object is a group") {
        val data: String = getStatementResource("data/sample-statement-object-is-group.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement =
          Statement(
            actor = sampleAgentActor,
            verb = sampleVerb,
            `object` = sampleGroupObject
          )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement where the object is a statement reference") {
        val data: String =
          getStatementResource("data/sample-statement-object-is-statement-ref.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleStatementRefObject
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement where the object is a sub-statement") {
        val data: String =
          getStatementResource("data/sample-statement-object-is-sub-statement.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleSubStatementObject
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement with a result object") {
        val data: String = getStatementResource("data/sample-statement-with-result.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          result = Some(sampleResult)
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement with a context object") {
        val data: String = getStatementResource("data/sample-statement-with-context.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          context = Some(sampleContext)
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement with a timestamp property") {
        val data: String = getStatementResource("data/sample-statement-with-timestamp.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          timestamp = Some(sampleOffsetDateTime)
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement with a stored property") {
        val data: String = getStatementResource("data/sample-statement-with-stored.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          timestamp = Some(sampleOffsetDateTime),
          stored = Some(sampleOffsetDateTime)
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement with an authority property") {
        val data: String = getStatementResource("data/sample-statement-with-authority.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          authority = Some(sampleAgentActor)
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement with a version property") {
        val data: String = getStatementResource("data/sample-statement-with-version.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          version = Some(XApiVersion(1, 0, Some(0)))
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it("should successfully decode a statement with an attachments property") {
        val data: String = getStatementResource("data/sample-statement-with-attachments.json")
        val decoded: Try[Statement] = Statement(data)
        val expected: Statement = Statement(
          actor = sampleAgentActor,
          verb = sampleVerb,
          `object` = sampleActivityObject,
          attachments = Some(List(sampleAttachment))
        )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)
      }

      it(
        "should successfully decode a complex statement that showcases most statement properties"
      ) {
        val data: String = getStatementResource("data/sample-statement-property-showcase.json")
        val decoded: Try[Statement] = Statement(data)
        assert(decoded.isSuccess)

        val actual: Statement = decoded.get
        assert(actual.id === Some(UUID.fromString("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")))
        actual.actor match {
          case group: Group => assert(group.member.isDefined)
          case _            => ()
        }
        assert(actual.result.isDefined)
        assert(actual.context.isDefined)
        assert(actual.context.get.contextActivities.isDefined)
        assert(actual.stored.isDefined)
        assert(actual.authority.isDefined)
        assert(actual.version.isDefined)
        assert(actual.`object`.value.isInstanceOf[Activity])
      }

      it("should successfully decode a statement following the cmi5 Community of Practice") {
        val data: String = getStatementResource("data/sample-statement-cmi5-example.json")
        val decoded: Try[Statement] = Statement(data)

        assert(decoded.isSuccess)

        val actual: Statement = decoded.get
        assert(actual.id === Some(UUID.fromString("2a41c918-b88b-4220-20a5-a4c32391a240")))
        assert(actual.result.isDefined)
        assert(actual.context.isDefined)
        assert(actual.context.get.contextActivities.isDefined)
        assert(actual.context.get.extensions.isDefined)
        assert(actual.timestamp === Some(OffsetDateTime.parse("2012-06-01T19:09:13.245+00:00")))
      }
    }

    describe("[validation]") {
      it(
        "should throw a validation error if a statement includes more than one signature attachment"
      ) {
        val exception = intercept[StatementValidationException] {
          Statement(
            actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
            verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verbs/test")),
            `object` =
              StatementObject(Activity(id = IRI("https://lrs.integralla.io/xapi/verbs/test"))),
            attachments = Some(
              List(
                Attachment(
                  usageType = IRI("http://adlnet.gov/expapi/attachments/signature"),
                  display = LanguageMap(Map("en-US" -> "Signature")),
                  description = Some(LanguageMap(Map("en-US" -> "A test signature"))),
                  contentType = "application/octet-stream",
                  length = 4235,
                  sha2 = "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
                ),
                Attachment(
                  usageType = IRI("http://adlnet.gov/expapi/attachments/signature"),
                  display = LanguageMap(Map("en-US" -> "Signature")),
                  description = Some(LanguageMap(Map("en-US" -> "A test signature"))),
                  contentType = "application/octet-stream",
                  length = 4235,
                  sha2 = "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
                )
              )
            )
          )
        }
        assert(
          exception.getMessage.contains("A signed statement can only have one signature attachment")
        )
      }
      it("should throw a validation error if the authority is an identified group") {
        val exception = intercept[StatementValidationException] {
          Statement(
            actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
            verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verbs/test")),
            `object` =
              StatementObject(Activity(id = IRI("https://lrs.integralla.io/xapi/verbs/test"))),
            authority = Some(
              Group(
                objectType = StatementObjectType.Group,
                mbox = Some(MBox("mailto:team.a@integralla.io"))
              )
            )
          )
        }
        assert(exception.getMessage.contains("An authority cannot be an identified group"))
      }
      it("should throw a validation error if the authority is a group with less then two members") {
        val exception = intercept[StatementValidationException] {
          Statement(
            actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
            verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verbs/test")),
            `object` =
              StatementObject(Activity(id = IRI("https://lrs.integralla.io/xapi/verbs/test"))),
            authority = Some(
              Group(
                objectType = StatementObjectType.Group,
                member = Some(
                  List(
                    Agent(account =
                      Some(Account("https://lrs.integralla.io/xapi/identity", "OAUTH_CONSUMER"))
                    )
                  )
                )
              )
            )
          )
        }
        assert(
          exception.getMessage.contains(
            "An authority represented as a group must have exactly two members"
          )
        )
      }
      it("should throw a validation error if the authority is a group with more then two members") {
        val exception = intercept[StatementValidationException] {
          Statement(
            actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
            verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verbs/test")),
            `object` =
              StatementObject(Activity(id = IRI("https://lrs.integralla.io/xapi/verbs/test"))),
            authority = Some(
              Group(
                objectType = StatementObjectType.Group,
                member = Some(
                  List(
                    Agent(account =
                      Some(Account("https://lrs.integralla.io/xapi/identity", "OAUTH_CONSUMER"))
                    ),
                    Agent(mbox = Some(MBox("mailto:test.1@integralla.io"))),
                    Agent(mbox = Some(MBox("mailto:test.2@integralla.io")))
                  )
                )
              )
            )
          )
        }
        assert(
          exception.getMessage.contains(
            "An authority represented as a group must have exactly two members"
          )
        )
      }
      it(
        "should throw a validation error if the authority is a group where no members are represented by an account"
      ) {
        val exception = intercept[StatementValidationException] {
          Statement(
            actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
            verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verbs/test")),
            `object` =
              StatementObject(Activity(id = IRI("https://lrs.integralla.io/xapi/verbs/test"))),
            authority = Some(
              Group(
                objectType = StatementObjectType.Group,
                member = Some(
                  List(
                    Agent(mbox = Some(MBox("mailto:test.1@integralla.io"))),
                    Agent(mbox = Some(MBox("mailto:test.2@integralla.io")))
                  )
                )
              )
            )
          )
        }
        assert(
          exception.getMessage.contains(
            "An OAuth consumer represented by an authority group member must be identified by account"
          )
        )
      }
      it(
        "should throw a validation error if the reserved verb for statement voiding is used when the statement object is not a statement ref"
      ) {
        val exception = intercept[StatementValidationException] {
          Statement(
            actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
            verb = StatementVerb(id = IRI("http://adlnet.gov/expapi/verbs/voided")),
            `object` = StatementObject(Agent(mbox = Some(MBox("mailto:test@integralla.io"))))
          )
        }
        assert(
          exception.getMessage.contains(
            """The reserved verb http://adlnet.gov/expapi/verbs/voided can only be used when the statement object is a statement reference"""
          )
        )
      }
      it(
        "should throw a validation error if the context.revision property is set when the statements object is not an activity"
      ) {
        val exception = intercept[StatementValidationException] {
          Statement(
            id = Some(UUID.fromString("12345678-1234-5678-1234-567812345678")),
            actor = Agent(
              objectType = Some(StatementObjectType.Agent),
              mbox = Some(MBox("mailto:xapi@adlnet.gov"))
            ),
            verb = StatementVerb(
              IRI("http://adlnet.gov/expapi/verbs/created"),
              Some(LanguageMap(Map("en-US" -> "created")))
            ),
            `object` = StatementObject(
              StatementRef(
                StatementObjectType.StatementRef,
                UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2")
              )
            ),
            context = Some(StatementContext(revision = Some("1.0.0")))
          )
        }
        assert(
          exception.getMessage.contains(
            """The "revision" property on the context object must only be used if the statement's object is an activity"""
          )
        )
      }

      it(
        "should throw a validation error if the context.platform property is set when the statements object is not an activity"
      ) {
        val exception = intercept[StatementValidationException] {
          Statement(
            id = Some(UUID.fromString("12345678-1234-5678-1234-567812345678")),
            actor = Agent(
              objectType = Some(StatementObjectType.Agent),
              mbox = Some(MBox("mailto:xapi@adlnet.gov"))
            ),
            verb = StatementVerb(
              IRI("http://adlnet.gov/expapi/verbs/created"),
              Some(LanguageMap(Map("en-US" -> "created")))
            ),
            `object` = StatementObject(
              StatementRef(
                StatementObjectType.StatementRef,
                UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2")
              )
            ),
            context = Some(StatementContext(platform = Some("lrp")))
          )
        }
        assert(
          exception.getMessage.contains(
            """The "platform" property on the context object must only be used if the statement's object is an activity"""
          )
        )
      }
    }

    describe("[equivalence]") {
      it("should return true if both statements are equivalent") {
        val left: Statement = getStatementFromResource("data/sample-statement-simplest.json")
        val right: Statement = getStatementFromResource("data/sample-statement-simplest.json")
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent, excepting id") {
        val left: Statement = getStatementFromResource("data/sample-statement-simplest.json")
        val right: Statement = getStatementFromResource("data/sample-statement-simplest.json")
          .copy(id = Some(UUID.randomUUID()))
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent, excepting authority") {
        val left: Statement = getStatementFromResource("data/sample-statement-simplest.json")
        val right: Statement = getStatementFromResource("data/sample-statement-simplest.json")
          .copy(authority =
            Some(
              Agent(
                Some(StatementObjectType.Agent),
                None,
                Some(MBox("mailto:xapi@adlnet.gov")),
                None,
                None,
                None
              )
            )
          )
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent, excepting stored") {
        val left: Statement = getStatementFromResource("data/sample-statement-simplest.json")
        val right: Statement = getStatementFromResource("data/sample-statement-simplest.json")
          .copy(stored = Some(OffsetDateTime.now(ZoneId.of("UTC"))))
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent, excepting timestamp") {
        val left: Statement = getStatementFromResource("data/sample-statement-simplest.json")
        val right: Statement = getStatementFromResource("data/sample-statement-simplest.json")
          .copy(timestamp = Some(OffsetDateTime.now(ZoneId.of("UTC"))))
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent, excepting version") {
        val left: Statement = getStatementFromResource("data/sample-statement-simplest.json")
        val right: Statement = getStatementFromResource("data/sample-statement-simplest.json")
          .copy(version = Some(XApiVersion(1, 0, Some(0))))
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent, excepting attachments") {
        val left: Statement =
          getStatementFromResource("data/sample-statement-with-attachments.json")
        val right: Statement =
          getStatementFromResource("data/sample-statement-with-attachments.json")
            .copy(attachments = None)
        assert(left.isEquivalentTo(right))
      }

      it("should return false if both statements are not equivalent") {
        val left: Statement = getStatementFromResource("data/sample-statement-simplest.json")
        val right: Statement = getStatementFromResource("data/sample-statement-simplest.json")
          .copy(actor =
            Agent(
              objectType = Some(StatementObjectType.Agent),
              mbox = Some(MBox("mailto:populus.tremuloides@integralla.io"))
            )
          )
        assert(left.isEquivalentTo(right) === false)
      }

      it("should return true if both statements are equivalent (actor is agent)") {
        val left: Statement = getStatementFromResource("data/sample-statement-actor-is-agent.json")
        val right: Statement = getStatementFromResource("data/sample-statement-actor-is-agent.json")
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent (actor is group)") {
        val left: Statement = getStatementFromResource("data/sample-statement-actor-is-group.json")
        val right: Statement = getStatementFromResource("data/sample-statement-actor-is-group.json")
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent (object is agent)") {
        val left: Statement = getStatementFromResource("data/sample-statement-object-is-agent.json")
        val right: Statement =
          getStatementFromResource("data/sample-statement-object-is-agent.json")
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent (object is group)") {
        val left: Statement = getStatementFromResource("data/sample-statement-object-is-group.json")
        val right: Statement =
          getStatementFromResource("data/sample-statement-object-is-group.json")
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent (object is statement reference)") {
        val left: Statement =
          getStatementFromResource("data/sample-statement-object-is-statement-ref.json")
        val right: Statement =
          getStatementFromResource("data/sample-statement-object-is-statement-ref.json")
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent (object is sub-statement)") {
        val left: Statement =
          getStatementFromResource("data/sample-statement-object-is-sub-statement.json")
        val right: Statement =
          getStatementFromResource("data/sample-statement-object-is-sub-statement.json")
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent (with attachments)") {
        val left: Statement =
          getStatementFromResource("data/sample-statement-with-attachments.json")
        val right: Statement =
          getStatementFromResource("data/sample-statement-with-attachments.json")
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent (CMI5 example)") {
        val left: Statement = getStatementFromResource("data/sample-statement-cmi5-example.json")
        val right: Statement = getStatementFromResource("data/sample-statement-cmi5-example.json")
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both statements are equivalent (property showcase example)") {
        val left: Statement =
          getStatementFromResource("data/sample-statement-property-showcase.json")
        val right: Statement =
          getStatementFromResource("data/sample-statement-property-showcase.json")
        assert(left.isEquivalentTo(right))
      }
    }

    describe("activityReferences") {
      it("should return a list with an activity if the statement object is an activity") {
        val statement: Statement = basicStatement.copy()
        val references: List[ActivityReference] = statement.activityReferences
        assert(references.length === 1)
        assert(references.head.referenceType === ActivityObjectRef)
        assert(references.head.inSubStatement === false)
      }

      it(
        "should return a list that includes context activities if context activities are defined"
      ) {
        val statement: Statement = basicStatement.copy(
          context = Some(sampleContext)
        )
        val references: List[ActivityReference] = statement.activityReferences
        assert(references.length === 2)
        assert(references.map(_.inSubStatement).forall(_ === false))
      }

      it(
        "should return an empty list if the statement object is not an activity, and no context activities are defined"
      ) {
        val statement =
          Statement(
            actor = sampleAgentActor,
            verb = sampleVerb,
            `object` = sampleAgentObject
          )
        val references: List[ActivityReference] = statement.activityReferences
        assert(references.isEmpty)
      }
    }

    describe("agentReferences") {

      it("should return all actors referenced by the statement") {
        val statement: Statement = Statement(
          id = Some(UUID.randomUUID()),
          actor = Agent(
            Some(StatementObjectType.Agent),
            Some("Populus Tremuloides"),
            Some(MBox("mailto:populus.tremuloides@integralla.io"))
          ),
          verb = StatementVerb(
            IRI("https://lrs.integralla.io/met"),
            Some(LanguageMap(Map("en-US" -> "met")))
          ),
          `object` = StatementObject(
            Agent(
              Some(StatementObjectType.Agent),
              Some("Prunus Persica"),
              Some(MBox("mailto:prunus.persica@integralla.io"))
            )
          ),
          context = Some(
            StatementContext(
              registration = None,
              instructor = Some(Agent(mbox = Some(MBox("mailto:instructors@integralla.io")))),
              team = Some(
                Group(
                  objectType = StatementObjectType.Group,
                  mbox = Some(MBox("mailto:team@integralla.io"))
                )
              )
            )
          ),
          authority = Some(
            Agent(mbox = Some(MBox("mailto:lrp@integralla.io")))
          ),
          version = None,
          attachments = None
        )

        val references: List[AgentReference] = statement.agentReferences
        assert(references.length === 5)

        assert(references.map(_.inSubStatement).forall(_ === false))

        val statementActor =
          references.find(_.agent.mbox.get.value === "mailto:populus.tremuloides@integralla.io").get
        assert(statementActor.referenceType === ActorRef)
        assert(statementActor.asGroupMember === false)

        val statementObject =
          references.find(_.agent.mbox.get.value === "mailto:prunus.persica@integralla.io").get
        assert(statementObject.referenceType === AgentObjectRef)
        assert(statementObject.asGroupMember === false)

        val instructor =
          references.find(_.agent.mbox.get.value === "mailto:instructors@integralla.io").get
        assert(instructor.referenceType === InstructorRef)
        assert(instructor.asGroupMember === false)

        val team = references.find(_.agent.mbox.get.value === "mailto:team@integralla.io").get
        assert(team.referenceType === TeamRef)
        assert(team.asGroupMember === false)

        val authority = references.find(_.agent.mbox.get.value === "mailto:lrp@integralla.io").get
        assert(authority.referenceType === AuthorityRef)
        assert(authority.asGroupMember === false)
      }
    }

    describe("getAttachments") {
      it("should return all attachments defined by the statement and/or its sub-statement") {
        val statement: Statement = Statement(
          id = Some(UUID.randomUUID()),
          actor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("Lorum Ipsum"),
            mbox = Some(MBox("mailto:lorum.ipsum@integralla.io")),
            mbox_sha1sum = None,
            openid = None,
            account = None
          ),
          verb = StatementVerb(IRI("https://lrs.integralla.io/verbs/test"), None),
          `object` = StatementObject(
            SubStatement(
              objectType = StatementObjectType.SubStatement,
              actor = Agent(
                objectType = Some(StatementObjectType.Agent),
                name = Some("Lorum Ipsum"),
                mbox = Some(MBox("mailto:lorum.ipsum@integralla.io")),
                mbox_sha1sum = None,
                openid = None,
                account = None
              ),
              verb = StatementVerb(IRI("https://lrs.integralla.io/verbs/test"), None),
              `object` = StatementObject(
                Activity(
                  Some(StatementObjectType.Activity),
                  IRI("https://lrs.integralla.io/activity/test"),
                  None
                )
              ),
              result = None,
              context = None,
              timestamp = None,
              attachments = Some(
                List(
                  Attachment(
                    IRI("https://example.com/attachment-usage/test"),
                    LanguageMap(Map("en-US" -> "Test Attachment", "it" -> "Allegato al test")),
                    Some(
                      LanguageMap(
                        Map("en-US" -> "A test attachment", "it" -> "Un allegato al test")
                      )
                    ),
                    "text/plain; charset=ascii",
                    27,
                    "495395e777cd98da653df9615d09c0fd6bb2f8d4788394cd53c56a3bfdcd848a",
                    None
                  )
                )
              )
            )
          ),
          result = None,
          context = None,
          timestamp = None,
          stored = None,
          authority = None,
          version = None,
          attachments = Some(
            List(
              Attachment(
                IRI("http://adlnet.gov/expapi/attachments/signature"),
                LanguageMap(Map("en-US" -> "Signature", "it" -> "Firma")),
                Some(LanguageMap(Map("en-US" -> "A test signature", "it" -> "Una firma di prova"))),
                "application/octet-stream",
                4235,
                "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634",
                None
              )
            )
          )
        )
        val attachments: List[Attachment] = statement.getAttachments
        assert(attachments.length === 2)
        assert(
          attachments
            .map(_.sha2).contains(
              "495395e777cd98da653df9615d09c0fd6bb2f8d4788394cd53c56a3bfdcd848a"
            )
        )
        assert(
          attachments
            .map(_.sha2).contains(
              "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
            )
        )
      }
      it(
        "should return an empty list if neither the statement, nor a sub-object, defines any attachments"
      ) {
        val statement: Statement = Statement(
          id = Some(UUID.randomUUID()),
          actor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("Lorum Ipsum"),
            mbox = Some(MBox("mailto:lorum.ipsum@integralla.io")),
            mbox_sha1sum = None,
            openid = None,
            account = None
          ),
          verb = StatementVerb(IRI("https://lrs.integralla.io/verbs/test"), None),
          `object` = StatementObject(
            Activity(
              Some(StatementObjectType.Activity),
              IRI("https://lrs.integralla.io/activity/test"),
              None
            )
          ),
          result = None,
          context = None,
          timestamp = None,
          stored = None,
          authority = None,
          version = None,
          attachments = None
        )
        val attachments: List[Attachment] = statement.getAttachments
        assert(attachments.isEmpty)
      }
    }

    describe("isVoidingStatement") {
      val base: Statement = Statement(
        id = Some(UUID.randomUUID()),
        actor = Agent(
          Some(StatementObjectType.Agent),
          Some("Populus Tremuloides"),
          Some(MBox("mailto:populus.tremuloides@integralla.io")),
          None,
          None,
          None
        ),
        verb = StatementVerb(IRI("http://adlnet.gov/expapi/verbs/voided"), None),
        `object` =
          StatementObject(StatementRef(StatementObjectType.StatementRef, UUID.randomUUID())),
        result = None,
        context = None,
        timestamp = None,
        stored = None,
        authority = None,
        version = None,
        attachments = None
      )

      it("should return true for a voiding statement") {
        val statement = base.copy()
        assert(statement.isVoidingStatement === true)
      }

      it("should return true for a non-voiding statement") {
        val statement = base.copy(
          verb = StatementVerb(IRI("http://adlnet.gov/expapi/verbs/attempted"), None)
        )
        assert(statement.isVoidingStatement === false)
      }
    }

    describe("signatureAttachment") {
      it("should return a signature attachment for a signed statement") {
        val statement: Statement = Statement(
          actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
          verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verbs/test")),
          `object` =
            StatementObject(Activity(id = IRI("https://lrs.integralla.io/xapi/verbs/test"))),
          attachments = Some(
            List(
              Attachment(
                usageType = IRI("http://adlnet.gov/expapi/attachments/signature"),
                display = LanguageMap(Map("en-US" -> "Signature")),
                description = Some(LanguageMap(Map("en-US" -> "A test signature"))),
                contentType = "application/octet-stream",
                length = 4235,
                sha2 = "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
              )
            )
          )
        )
        val signature: Option[Attachment] = statement.signatureAttachment
        assert(signature.isDefined)
      }
      it("should return none for an un-signed statement") {
        val statement: Statement = Statement(
          actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
          verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verbs/test")),
          `object` =
            StatementObject(Activity(id = IRI("https://lrs.integralla.io/xapi/verbs/test")))
        )
        val signature: Option[Attachment] = statement.signatureAttachment
        assert(signature.isEmpty)
      }
    }

    describe("size") {
      it("should return the size of the encoded statement in bytes") {
        val statement: Statement = Statement(
          id = Some(UUID.randomUUID()),
          actor = Agent(
            Some(StatementObjectType.Agent),
            Some("Populus Tremuloides"),
            Some(MBox("mailto:populus.tremuloides@integralla.io"))
          ),
          verb = StatementVerb(IRI("http://adlnet.gov/expapi/verbs/voided"), None),
          `object` =
            StatementObject(StatementRef(StatementObjectType.StatementRef, UUID.randomUUID())),
          result = None,
          context = None,
          timestamp = None,
          stored = None,
          authority = None,
          version = None,
          attachments = None
        )
        assert(statement.size === 292)
      }
    }
  }
}
