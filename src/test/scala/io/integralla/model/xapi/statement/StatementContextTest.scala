package io.integralla.model.xapi.statement

import io.circe.Json
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.references.{AgentReference, InstructorRef, TeamRef}
import io.integralla.model.xapi.common.ExtensionMap
import io.integralla.model.xapi.statement.identifiers.{Account, IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import java.util.UUID

class StatementContextTest extends UnitSpec {

  val sampleContext: StatementContext = StatementContext(
    registration = Some(UUID.fromString("1fc289d4-5061-4635-8500-ada2009fb7c8")),
    instructor =
      Some(Agent(objectType = Some(StatementObjectType.Agent), mbox = Some(MBox("mailto:instructor@integralla.io")))),
    team = Some(
      Group(
        objectType = StatementObjectType.Group,
        name = Some("Team A"),
        mbox = Some(MBox("mailto:team.a@integralla.io")),
        member = Some(
          List(
            Agent(mbox = Some(MBox("mailto:a@integralla.io"))),
            Agent(mbox = Some(MBox("mailto:b@integralla.io")))
          )
        )
      )
    ),
    contextActivities = Some(
      ContextActivities(
        parent = Some(List(Activity(id = IRI("https://lrs.integralla.io/activity/parent")))),
        grouping = Some(List(Activity(id = IRI("https://lrs.integralla.io/activity/grouping")))),
        category = Some(List(Activity(id = IRI("https://lrs.integralla.io/activity/category")))),
        other = Some(List(Activity(id = IRI("https://lrs.integralla.io/activity/other"))))
      )
    ),
    revision = Some("1.0.0"),
    platform = Some("Integralla"),
    language = Some("en-UK"),
    statement = None,
    extensions = Some(
      ExtensionMap(Map(IRI("http://id.tincanapi.com/extension/topic") -> "testing".asJson))
    )
  )

  describe("Statement Context") {
    describe("[encoding/decoding]") {
      it("should successfully encode/decode a context object") {
        val expected: String =
          """{
            |  "registration" : "1fc289d4-5061-4635-8500-ada2009fb7c8",
            |  "instructor" : {
            |    "objectType" : "Agent",
            |    "mbox" : "mailto:instructor@integralla.io"
            |  },
            |  "team" : {
            |    "objectType" : "Group",
            |    "name" : "Team A",
            |    "mbox" : "mailto:team.a@integralla.io",
            |    "member" : [
            |      {
            |        "mbox" : "mailto:a@integralla.io"
            |      },
            |      {
            |        "mbox" : "mailto:b@integralla.io"
            |      }
            |    ]
            |  },
            |  "contextActivities" : {
            |    "parent" : [
            |      {
            |        "id" : "https://lrs.integralla.io/activity/parent"
            |      }
            |    ],
            |    "grouping" : [
            |      {
            |        "id" : "https://lrs.integralla.io/activity/grouping"
            |      }
            |    ],
            |    "category" : [
            |      {
            |        "id" : "https://lrs.integralla.io/activity/category"
            |      }
            |    ],
            |    "other" : [
            |      {
            |        "id" : "https://lrs.integralla.io/activity/other"
            |      }
            |    ]
            |  },
            |  "revision" : "1.0.0",
            |  "platform" : "Integralla",
            |  "language" : "en-UK",
            |  "extensions" : {
            |    "http://id.tincanapi.com/extension/topic" : "testing"
            |  }
            |}""".stripMargin

        val encoded: String = sampleContext.asJson.spaces2
        assert(encoded === expected)
        val decoded: Option[StatementContext] = decode[StatementContext](encoded).toOption
        assert(decoded.get === sampleContext)
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
        mbox_sha1sum = None,
        member = Some(
          List(
            Agent(None, None, Some(MBox("mailto:picea.engelmannii@integralla.io")), None, None, None),
            Agent(None, None, Some(MBox("mailto:platanus.acerifolia@integralla.io")), None, None, None)
          )
        )
      )

      it("should return a list of agent references (instructor as agent, no team)") {
        val context: StatementContext = sampleContext.copy(team = None)
        val references: List[AgentReference] = context.getAgentReferences(inSubStatement = false)
        assert(references.length === 1)
        assert(references.head.referenceType === InstructorRef)
        assert(references.head.inSubStatement === false)
        assert(references.head.asGroupMember === false)
      }

      it("should return a list of agent references (instructor as identified group w/o members, no team)") {
        val context: StatementContext = sampleContext.copy(
          instructor = Some(
            group.copy(mbox = Some(MBox("mailto:instructors@integralla.io")), member = None)
          ),
          team = None
        )
        val references: List[AgentReference] = context.getAgentReferences(inSubStatement = false)
        assert(references.length === 1)
        assert(references.head.referenceType === InstructorRef)
        assert(references.head.inSubStatement === false)
        assert(references.head.asGroupMember === false)
      }

      it("should return a list of agent references (instructor as identified group w/ members, no team)") {
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
      it("should return a list of agent references (instructor as anonymous group w/ members)") {
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

      it("should return a list of agent references (instructor as agent, team as identified group)") {
        val context: StatementContext = sampleContext.copy()
        val references: List[AgentReference] = context.getAgentReferences(inSubStatement = false)
        assert(references.length === 4)
        assert(references.forall(_.inSubStatement === false))

        assert(references.count(_.referenceType === InstructorRef) === 1)
        assert(references.count(_.referenceType === TeamRef) === 3)

        assert(references.filter(_.referenceType === InstructorRef).forall(_.asGroupMember === false))
        assert(references.count(ref => ref.referenceType == TeamRef && !ref.asGroupMember) === 1)
        assert(references.count(ref => ref.referenceType == TeamRef && ref.asGroupMember) === 2)
      }

      it("should return a list of agent references (instructor as agent, team as anonymous group)") {
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
