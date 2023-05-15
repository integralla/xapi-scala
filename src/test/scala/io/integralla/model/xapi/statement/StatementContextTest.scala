package io.integralla.model.xapi.statement

import io.circe.Json
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.references.{AgentReference, InstructorRef, TeamRef}
import io.integralla.model.xapi.common.ExtensionMap
import io.integralla.model.xapi.statement.identifiers.{Account, IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import java.util.UUID
import scala.io.Source
import scala.util.Using

class StatementContextTest extends UnitSpec {

  val sampleInstructor: Agent = Agent(
    Some(StatementObjectType.Agent),
    Some("Andrew Downes"),
    None,
    None,
    None,
    Some(Account("http://www.example.com", "13936749"))
  )
  val sampleTeam: Group =
    Group(StatementObjectType.Group, Some("Team PB"), Some(MBox("mailto:teampb@example.com")), None, None, None, None)

  val sampleParent: List[Activity] = List(
    Activity(Some(StatementObjectType.Activity), IRI("http://www.example.com/meetings/series/267"), None)
  )

  val sampleCategory: List[Activity] = List(
    Activity(
      Some(StatementObjectType.Activity),
      IRI("http://www.example.com/meetings/categories/teammeeting"),
      Some(
        ActivityDefinition(
          Some(LanguageMap(Map("en" -> "team meeting"))),
          Some(LanguageMap(Map("en" -> "A category of meeting used for regular team meetings."))),
          Some(IRI("http://example.com/expapi/activities/meetingcategory")),
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
  )

  val sampleOther: List[Activity] = List(
    Activity(Some(StatementObjectType.Activity), IRI("http://www.example.com/meetings/occurances/34257"), None),
    Activity(Some(StatementObjectType.Activity), IRI("http://www.example.com/meetings/occurances/3425567"), None)
  )

  val sampleContextActivities: ContextActivities = ContextActivities(
    Some(sampleParent),
    None,
    Some(sampleCategory),
    Some(sampleOther)
  )

  val sampleStatementRef: StatementRef =
    StatementRef(StatementObjectType.StatementRef, UUID.fromString("6690e6c9-3ef0-4ed3-8b37-7f3964730bee"))

  val sampleExtensionValue: Json = Map("name" -> "Kilby", "id" -> "http://example.com/rooms/342").asJson
  val sampleExtensions: ExtensionMap = ExtensionMap(
    Map(
      IRI("http://example.com/profiles/meetings/activitydefinitionextensions/room") -> sampleExtensionValue
    )
  )

  val sampleContext: StatementContext = StatementContext(
    Some(UUID.fromString("ec531277-b57b-4c15-8d91-d292c5b2b8f7")),
    Some(sampleInstructor),
    Some(sampleTeam),
    Some(sampleContextActivities),
    Some("1.0.0"),
    Some("Example virtual meeting software"),
    Some("tlh"),
    Some(sampleStatementRef),
    Some(sampleExtensions)
  )

  val sampleContextEncoded: String = Using.resource(Source.fromResource("data/sample-context-object.json"))(_.mkString)

  describe("Statement Context") {
    describe("[encoding]") {
      it("should successfully encode a context object") {
        val actual: String = sampleContext.asJson.spaces2
        assert(actual === sampleContextEncoded)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a context object") {
        val decoded: Either[io.circe.Error, StatementContext] = decode[StatementContext](sampleContextEncoded)
        decoded match {
          case Right(actual) => assert(actual === sampleContext)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }
    }

    describe("[equivalence]") {
      it("should return true if both objects are equivalent") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return false if both objects are not equivalent") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy(registration = Some(UUID.randomUUID()))
        assert(left.isEquivalentTo(right) === false)
      }
    }

    describe("getAgentReferences") {
      val group: Group = Group(
        objectType = StatementObjectType.Group,
        name = None,
        mbox = None,
        mbox_sha1sum = None,
        openid = None,
        account = None,
        member = Some(
          List(
            Agent(None, None, Some(MBox("mailto:picea.engelmannii@integralla.io")), None, None, None),
            Agent(None, None, Some(MBox("mailto:platanus.acerifolia@integralla.io")), None, None, None)
          )
        )
      )

      it(
        "should return a list with a single agent reference if the instructor is an agent, and a team is not defined"
      ) {
        val context: StatementContext = sampleContext.copy(team = None)
        val references: List[AgentReference] = context.getAgentReferences(inSubStatement = false)
        assert(references.length === 1)
        assert(references.head.referenceType === InstructorRef)
        assert(references.head.inSubStatement === false)
        assert(references.head.asGroupMember === false)
      }

      it(
        "should return a list with a single agent reference if the instructor is an identified group without members, and a team is not defined"
      ) {
        val context: StatementContext = sampleContext.copy(
          instructor = Some(
            group.copy(
              mbox = Some(MBox("mailto:instructors@integralla.io")),
              member = None
            )
          ),
          team = None
        )
        val references: List[AgentReference] = context.getAgentReferences(inSubStatement = false)
        assert(references.length === 1)
        assert(references.head.referenceType === InstructorRef)
        assert(references.head.inSubStatement === false)
        assert(references.head.asGroupMember === false)
      }

      it(
        "should return a list with a multiple actors if the instructor is an identified group with members, and a team is not defined"
      ) {
        val context: StatementContext = sampleContext.copy(
          instructor = Some(
            group.copy(
              mbox = Some(MBox("mailto:instructors@integralla.io"))
            )
          ),
          team = None
        )
        val references: List[AgentReference] = context.getAgentReferences(inSubStatement = false)
        assert(references.length === 3)

        val instructors = references.find(_.agent.mbox.get.value === "mailto:instructors@integralla.io").get
        assert(instructors.referenceType === InstructorRef)
        assert(instructors.asGroupMember === false)

        val members = references.filter(_.agent.mbox.get.value !== "mailto:instructors@integralla.io")
        assert(members.forall(_.asGroupMember === true))

        assert(references.forall(_.inSubStatement === false))

      }
      it(
        "should return a list with multiple actors if the instructor is an anonymous group with members, and a team is not defined"
      ) {
        val context: StatementContext = sampleContext.copy(
          instructor = Some(group),
          team = None
        )
        val references: List[AgentReference] = context.getAgentReferences(inSubStatement = false)
        assert(references.length === 2)
        assert(references.forall(_.referenceType === InstructorRef))
        assert(references.forall(_.inSubStatement === false))
        assert(references.forall(_.asGroupMember === true))
      }

      it("should return a list with multiple actors if the instructor and team are defined (identified group)") {
        val context: StatementContext = sampleContext.copy()
        val references: List[AgentReference] = context.getAgentReferences(inSubStatement = false)
        assert(references.length === 2)
        assert(references.forall(_.inSubStatement === false))

        assert(references.count(_.referenceType === InstructorRef) === 1)
        assert(references.count(_.referenceType === TeamRef) === 1)

        assert(references.filter(_.referenceType === InstructorRef).forall(_.asGroupMember === false))
        assert(references.filter(_.referenceType === TeamRef).forall(_.asGroupMember === false))
      }

      it("should return a list with multiple actors if the instructor and team are defined (anonymous group)") {
        val context: StatementContext = sampleContext.copy(
          team = Some(group)
        )
        val references: List[AgentReference] = context.getAgentReferences(inSubStatement = false)
        assert(references.length === 3)
        assert(references.forall(_.inSubStatement === false))

        assert(references.count(_.referenceType === InstructorRef) === 1)
        assert(references.count(_.referenceType === TeamRef) === 2)

        assert(references.filter(_.referenceType === InstructorRef).forall(_.asGroupMember === false))
        assert(references.filter(_.referenceType === TeamRef).forall(_.asGroupMember === true))
      }

      it("should return an empty list if neither the instructor or team are defined") {
        val context: StatementContext = sampleContext.copy(
          instructor = None,
          team = None
        )
        assert(context.getAgentReferences(inSubStatement = false).isEmpty)
      }
    }
  }
}
