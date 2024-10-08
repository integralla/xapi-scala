package io.integralla.xapi.model

import io.integralla.xapi.model.exceptions.StatementValidationException
import io.integralla.xapi.model.references._
import org.scalatest.funspec.AnyFunSpec

import java.util.UUID
import scala.io.Source
import scala.util.{Try, Using}

class SubStatementTest extends AnyFunSpec {

  /* Activity SubStatement */
  val sampleActivitySubStatement: SubStatement = SubStatement(
    StatementObjectType.SubStatement,
    Agent(
      Some(StatementObjectType.Agent),
      None,
      Some(MBox("mailto:test@example.com")),
      None,
      None,
      None
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

  val sampleActivitySubStatementEncoded: String =
    Using.resource(Source.fromResource("data/sample-sub-statement-object-is-activity.json"))(
      _.mkString
    )

  /* Agent SubStatement */
  val sampleAgentSubStatement: SubStatement = SubStatement(
    StatementObjectType.SubStatement,
    Agent(
      Some(StatementObjectType.Agent),
      None,
      Some(MBox("mailto:test@example.com")),
      None,
      None,
      None
    ),
    StatementVerb(
      IRI("http://example.com/visited"),
      Some(LanguageMap(Map("en-US" -> "will visit")))
    ),
    StatementObject(
      Agent(
        Some(StatementObjectType.Agent),
        Some("Andrew Downes"),
        Some(MBox("mailto:andrew@example.co.uk"))
      )
    )
  )
  val sampleAgentSubStatementEncoded: String =
    Using.resource(Source.fromResource("data/sample-sub-statement-object-is-agent.json"))(
      _.mkString
    )

  /* Group SubStatement */
  val sampleGroupSubStatement: SubStatement = SubStatement(
    StatementObjectType.SubStatement,
    Agent(
      Some(StatementObjectType.Agent),
      None,
      Some(MBox("mailto:test@example.com")),
      None,
      None,
      None
    ),
    StatementVerb(
      IRI("http://example.com/visited"),
      Some(LanguageMap(Map("en-US" -> "will visit")))
    ),
    StatementObject(
      Group(
        StatementObjectType.Group,
        Some("Example Group"),
        None,
        None,
        None,
        Some(Account("http://example.com/homePage", "GroupAccount")),
        Some(
          List(
            Agent(
              Some(StatementObjectType.Agent),
              Some("Andrew Downes"),
              Some(MBox("mailto:andrew@example.co.uk")),
              None,
              None,
              None
            ),
            Agent(
              Some(StatementObjectType.Agent),
              Some("Aaron Silvers"),
              None,
              None,
              Some("http://aaron.openid.example.org"),
              None
            )
          )
        )
      )
    ),
    None,
    None,
    None,
    None
  )
  val sampleGroupSubStatementEncoded: String =
    Using.resource(Source.fromResource("data/sample-sub-statement-object-is-group.json"))(
      _.mkString
    )

  /* StatementRef SubStatement */
  val sampleStatementRefSubStatement: SubStatement = SubStatement(
    StatementObjectType.SubStatement,
    Agent(
      Some(StatementObjectType.Agent),
      None,
      Some(MBox("mailto:test@example.com")),
      None,
      None,
      None
    ),
    StatementVerb(
      IRI("http://example.com/visited"),
      Some(LanguageMap(Map("en-US" -> "will visit")))
    ),
    StatementObject(
      StatementRef(
        StatementObjectType.StatementRef,
        UUID.fromString("f1dc3573-e346-4bd0-b295-f5dde5cbe13f")
      )
    ),
    None,
    None,
    None,
    None
  )
  val sampleStatementRefSubStatementEncoded: String =
    Using.resource(Source.fromResource("data/sample-sub-statement-object-is-statement-ref.json"))(
      _.mkString
    )

  describe("SubStatement") {
    describe("[validation]") {
      it(
        "should throw a validation exception if the sub-statement includes a nested sub-statement"
      ) {
        val exception = intercept[StatementValidationException] {
          SubStatement(
            objectType = StatementObjectType.SubStatement,
            actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
            verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verb/test")),
            `object` = StatementObject(
              SubStatement(
                objectType = StatementObjectType.SubStatement,
                actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
                verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verb/test")),
                `object` = StatementObject(
                  Activity(id = IRI("https://lrs.integralla.io/xapi/activity/test"))
                )
              )
            )
          )
        }
        assert(
          exception.getMessage.contains(
            """A sub-statement cannot contain a sub-statement of it's own"""
          )
        )
      }
      it(
        "should throw a validation exception if the context includes the revision property and the object is not an activity"
      ) {
        val exception = intercept[StatementValidationException] {
          SubStatement(
            objectType = StatementObjectType.SubStatement,
            actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
            verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verb/test")),
            `object` = StatementObject(Agent(mbox = Some(MBox("mailto:test@integralla.io")))),
            context = Some(
              StatementContext(
                revision = Some("1.0.0")
              )
            )
          )
        }
        assert(
          exception.getMessage.contains(
            """The "revision" property on the context object must only be used if the statement's object is an activity"""
          )
        )
      }
      it(
        "should throw a validation exception if the context includes the platform property and the object is not an activity"
      ) {
        val exception = intercept[StatementValidationException] {
          SubStatement(
            objectType = StatementObjectType.SubStatement,
            actor = Agent(mbox = Some(MBox("mailto:test@integralla.io"))),
            verb = StatementVerb(id = IRI("https://lrs.integralla.io/xapi/verb/test")),
            `object` = StatementObject(Agent(mbox = Some(MBox("mailto:test@integralla.io")))),
            context = Some(
              StatementContext(
                platform = Some("lrp")
              )
            )
          )
        }
        assert(
          exception.getMessage.contains(
            """The "platform" property on the context object must only be used if the statement's object is an activity"""
          )
        )
      }
    }

    describe("[encoding]") {
      it("should successfully encode a sub-statement (activity)") {
        val actual: String = sampleActivitySubStatement.toJson(spaces = true)
        assert(actual === sampleActivitySubStatementEncoded)
      }

      it("should successfully encode a sub-statement (agent)") {
        val actual: String = sampleAgentSubStatement.toJson(spaces = true)
        assert(actual === sampleAgentSubStatementEncoded)
      }

      it("should successfully encode a sub-statement (group)") {
        val actual: String = sampleGroupSubStatement.toJson(spaces = true)
        assert(actual === sampleGroupSubStatementEncoded)
      }

      it("should successfully encode a sub-statement (statement reference)") {
        val actual: String = sampleStatementRefSubStatement.toJson(spaces = true)
        assert(actual === sampleStatementRefSubStatementEncoded)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a sub-statement (activity)") {
        val decoded: Try[SubStatement] = SubStatement(sampleActivitySubStatementEncoded)
        assert(decoded.isSuccess)
        assert(decoded.get === sampleActivitySubStatement)
      }

      it("should successfully decode a sub-statement (agent)") {
        val decoded: Try[SubStatement] = SubStatement(sampleAgentSubStatementEncoded)
        assert(decoded.isSuccess)
        assert(decoded.get === sampleAgentSubStatement)
      }

      it("should successfully decode a sub-statement (group)") {
        val decoded: Try[SubStatement] = SubStatement(sampleGroupSubStatementEncoded)
        assert(decoded.isSuccess)
        assert(decoded.get === sampleGroupSubStatement)
      }

      it("should successfully decode a sub-statement (statement reference)") {
        val decoded: Try[SubStatement] = SubStatement(sampleStatementRefSubStatementEncoded)
        assert(decoded.isSuccess)
        assert(decoded.get === sampleStatementRefSubStatement)
      }
    }

    describe("[equivalence]") {
      it("should return true if both sub-statements are equivalent (activity object)") {
        val left: SubStatement = sampleActivitySubStatement.copy()
        val right: SubStatement = sampleActivitySubStatement.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both sub-statements are equivalent (agent object)") {
        val left: SubStatement = sampleAgentSubStatement.copy()
        val right: SubStatement = sampleAgentSubStatement.copy()
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both sub-statements are equivalent (group object)") {
        val left: SubStatement = sampleGroupSubStatement.copy()
        val right: SubStatement = sampleGroupSubStatement.copy()
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both sub-statements are equivalent (statement ref object)") {
        val left: SubStatement = sampleStatementRefSubStatement.copy()
        val right: SubStatement = sampleStatementRefSubStatement.copy()
        assert(left.isEquivalentTo(right))
      }

      it("should return false if both sub-statements are not equivalent") {
        val left: SubStatement = sampleActivitySubStatement.copy()
        val right: SubStatement = sampleActivitySubStatement.copy(
          verb = StatementVerb(
            IRI("http://example.com/observed"),
            Some(LanguageMap(Map("en-US" -> "observed")))
          )
        )
        assert(left.isEquivalentTo(right) === false)
      }

      it("should return false if both sub-statements have different object types") {
        val left: SubStatement = sampleActivitySubStatement.copy()
        val right: SubStatement = sampleAgentSubStatement.copy()
        assert(left.isEquivalentTo(right) === false)
      }
    }

    describe("activityReferences") {
      it("should return an activity reference if the statement object is an activity") {
        val subStatement: SubStatement = sampleActivitySubStatement.copy()
        val references: List[ActivityReference] = subStatement.activityReferences
        assert(references.length === 1)
        assert(references.head.referenceType === ActivityObjectRef)
        assert(references.head.inSubStatement === true)
      }

      it(
        "should return a list that includes context activities if context activities are defined"
      ) {
        val subStatement: SubStatement = sampleActivitySubStatement.copy(
          context = Some(
            StatementContext(
              registration = None,
              instructor = None,
              team = None,
              contextActivities = Some(
                ContextActivities(
                  parent = Some(
                    List(Activity(None, IRI("https://lrs.integralla.io/activity/parent"), None))
                  ),
                  grouping = Some(
                    List(Activity(None, IRI("https://lrs.integralla.io/activity/grouping"), None))
                  ),
                  category = Some(
                    List(Activity(None, IRI("https://lrs.integralla.io/activity/category"), None))
                  ),
                  other = Some(
                    List(Activity(None, IRI("https://lrs.integralla.io/activity/other"), None))
                  )
                )
              ),
              revision = None,
              platform = None,
              language = None,
              statement = None,
              extensions = None
            )
          )
        )
        val references: List[ActivityReference] = subStatement.activityReferences
        assert(references.length === 5)
        assert(references.map(_.inSubStatement).forall(_ === true))
      }

      it(
        "should return an empty list if the statement object is not an activity, and if no context activities are defined"
      ) {
        val subStatement: SubStatement = sampleAgentSubStatement.copy()
        val references: List[ActivityReference] = subStatement.activityReferences
        assert(references.isEmpty)
      }
    }

    describe("agentReferences") {
      val subStatement: SubStatement = SubStatement(
        objectType = StatementObjectType.SubStatement,
        actor = Agent(
          Some(StatementObjectType.Agent),
          Some("Populus Tremuloides"),
          Some(MBox("mailto:populus.tremuloides@integralla.io")),
          None,
          None,
          None
        ),
        verb = StatementVerb(
          IRI("https://lrs.integralla.io/met"),
          Some(LanguageMap(Map("en-US" -> "met")))
        ),
        `object` = StatementObject(
          Agent(
            Some(StatementObjectType.Agent),
            Some("Prunus Persica"),
            Some(MBox("mailto:prunus.persica@integralla.io")),
            None,
            None,
            None
          )
        ),
        result = None,
        context = Some(
          StatementContext(
            registration = None,
            instructor = Some(
              Agent(None, None, Some(MBox("mailto:instructors@integralla.io")), None, None, None)
            ),
            team = Some(
              Group(
                StatementObjectType.Group,
                None,
                Some(MBox("mailto:team@integralla.io")),
                None,
                None,
                None,
                None
              )
            ),
            contextActivities = None,
            revision = None,
            platform = None,
            language = None,
            statement = None,
            extensions = None
          )
        ),
        timestamp = None,
        attachments = None
      )
      it("should return a list of actors referenced in the actor, object, and context properties") {
        val statement: SubStatement = subStatement.copy()
        val references: List[AgentReference] = statement.agentReferences
        assert(references.length === 4)
        assert(references.map(_.inSubStatement).forall(_ === true))

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
      }

      it(
        "should return a list of actors referenced in the actor, object, and context properties (context undefined)"
      ) {
        val statement: SubStatement = subStatement.copy(context = None)
        val references: List[AgentReference] = statement.agentReferences
        assert(references.length === 2)
      }

      it(
        "should return a list of actors referenced in the actor, object, and context properties (object has no references)"
      ) {
        val statement: SubStatement = subStatement.copy(`object` =
          StatementObject(
            StatementRef(objectType = StatementObjectType.StatementRef, id = UUID.randomUUID())
          )
        )
        val references: List[AgentReference] = statement.agentReferences
        assert(references.length === 3)
        assert(!references.exists(_.agent.mbox.get.value === "mailto:prunus.persica@integralla.io"))
      }
    }
  }
}
