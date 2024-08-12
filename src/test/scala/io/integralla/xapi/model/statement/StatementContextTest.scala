package io.integralla.xapi.model.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.xapi.model.common.ExtensionMap
import io.integralla.xapi.model.references._
import org.scalatest.funspec.AnyFunSpec

import java.util.UUID

class StatementContextTest extends AnyFunSpec {

  val sampleContext: StatementContext = StatementContext(
    registration = Some(UUID.fromString("1fc289d4-5061-4635-8500-ada2009fb7c8")),
    instructor = Some(
      Agent(
        objectType = Some(StatementObjectType.Agent),
        mbox = Some(MBox("mailto:instructor@integralla.io"))
      )
    ),
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
      it("should successfully encode/decode a context object with context agents [xAPI 2.0]") {
        val context: StatementContext = StatementContext(
          contextAgents = Some(
            List(
              ContextAgent(
                objectType = ContextAgent.contextType,
                agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
                relevantTypes = Some(
                  List(
                    IRI("https://lrs.integralla.io/types/instructor"),
                    IRI("https://lrs.integralla.io/types/subject-matter-expert")
                  )
                )
              )
            )
          )
        )

        val expected: String =
          """{
            |  "contextAgents" : [
            |    {
            |      "objectType" : "contextAgent",
            |      "agent" : {
            |        "mbox" : "mailto:context.agent@example.com"
            |      },
            |      "relevantTypes" : [
            |        "https://lrs.integralla.io/types/instructor",
            |        "https://lrs.integralla.io/types/subject-matter-expert"
            |      ]
            |    }
            |  ]
            |}""".stripMargin

        val encoded: String = context.asJson.spaces2
        assert(encoded === expected)
        val decoded: Option[StatementContext] = decode[StatementContext](encoded).toOption
        assert(decoded.get === context)
      }
      it("should successfully encode/decode a context object with context groups [xAPI 2.0]") {
        val context: StatementContext = StatementContext(
          contextGroups = Some(
            List(
              ContextGroup(
                objectType = ContextGroup.contextType,
                group = Group(
                  objectType = StatementObjectType.Group,
                  name = Some("Identified Group"),
                  mbox = Some(MBox("mailto:identified.group@integralla.io")),
                  member = Some(
                    List(
                      Agent(mbox = Some(MBox("mailto:member.one@example.com"))),
                      Agent(mbox = Some(MBox("mailto:member.two@example.com")))
                    )
                  )
                ),
                relevantTypes = Some(
                  List(
                    IRI("https://lrs.integralla.io/types/team"),
                    IRI("https://lrs.integralla.io/types/leads")
                  )
                )
              )
            )
          )
        )

        val expected: String =
          """{
            |  "contextGroups" : [
            |    {
            |      "objectType" : "contextGroup",
            |      "group" : {
            |        "objectType" : "Group",
            |        "name" : "Identified Group",
            |        "mbox" : "mailto:identified.group@integralla.io",
            |        "member" : [
            |          {
            |            "mbox" : "mailto:member.one@example.com"
            |          },
            |          {
            |            "mbox" : "mailto:member.two@example.com"
            |          }
            |        ]
            |      },
            |      "relevantTypes" : [
            |        "https://lrs.integralla.io/types/team",
            |        "https://lrs.integralla.io/types/leads"
            |      ]
            |    }
            |  ]
            |}""".stripMargin

        val encoded: String = context.asJson.spaces2
        assert(encoded === expected)
        val decoded: Option[StatementContext] = decode[StatementContext](encoded).toOption
        assert(decoded.get === context)
      }
    }

    describe("[equivalence]") {
      it("should return true if both objects are equivalent") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both objects are equivalent (contextAgents) [xAPI 2.0]") {
        val left: StatementContext = StatementContext(
          contextAgents = Some(
            List(
              ContextAgent(
                objectType = ContextAgent.contextType,
                agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
                relevantTypes = Some(
                  List(
                    IRI("https://lrs.integralla.io/types/instructor"),
                    IRI("https://lrs.integralla.io/types/subject-matter-expert")
                  )
                )
              ),
              ContextAgent(
                objectType = ContextAgent.contextType,
                agent = Agent(mbox = Some(MBox("mailto:other.agent@example.com")))
              )
            )
          )
        )
        val right: StatementContext = StatementContext(
          contextAgents = Some(
            List(
              ContextAgent(
                objectType = ContextAgent.contextType,
                agent = Agent(mbox = Some(MBox("mailto:other.agent@example.com")))
              ),
              ContextAgent(
                objectType = ContextAgent.contextType,
                agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
                relevantTypes = Some(
                  List(
                    IRI("https://lrs.integralla.io/types/instructor"),
                    IRI("https://lrs.integralla.io/types/subject-matter-expert")
                  )
                )
              )
            )
          )
        )
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both objects are equivalent (contextGroups) [xAPI 2.0]") {
        val left: StatementContext = StatementContext(
          contextGroups = Some(
            List(
              ContextGroup(
                objectType = ContextGroup.contextType,
                group = Group(
                  objectType = StatementObjectType.Group,
                  name = Some("Identified Group"),
                  mbox = Some(MBox("mailto:identified.group@integralla.io")),
                  member = Some(
                    List(
                      Agent(mbox = Some(MBox("mailto:member.one@example.com"))),
                      Agent(mbox = Some(MBox("mailto:member.two@example.com")))
                    )
                  )
                ),
                relevantTypes = Some(
                  List(
                    IRI("https://lrs.integralla.io/types/team"),
                    IRI("https://lrs.integralla.io/types/leads")
                  )
                )
              ),
              ContextGroup(
                objectType = ContextGroup.contextType,
                group = Group(
                  objectType = StatementObjectType.Group,
                  mbox = Some(MBox("mailto:other.group@integralla.io"))
                )
              )
            )
          )
        )
        val right: StatementContext = StatementContext(
          contextGroups = Some(
            List(
              ContextGroup(
                objectType = ContextGroup.contextType,
                group = Group(
                  objectType = StatementObjectType.Group,
                  mbox = Some(MBox("mailto:other.group@integralla.io"))
                )
              ),
              ContextGroup(
                objectType = ContextGroup.contextType,
                group = Group(
                  objectType = StatementObjectType.Group,
                  name = Some("Identified Group"),
                  mbox = Some(MBox("mailto:identified.group@integralla.io")),
                  member = Some(
                    List(
                      Agent(mbox = Some(MBox("mailto:member.one@example.com"))),
                      Agent(mbox = Some(MBox("mailto:member.two@example.com")))
                    )
                  )
                ),
                relevantTypes = Some(
                  List(
                    IRI("https://lrs.integralla.io/types/team"),
                    IRI("https://lrs.integralla.io/types/leads")
                  )
                )
              )
            )
          )
        )
        assert(left.isEquivalentTo(right))
      }
      it("should return false if the objects are not equivalent (registration)") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy(registration = Some(UUID.randomUUID()))
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (instructor)") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy(instructor = None)
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (team)") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy(team = None)
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (contextActivities)") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy(contextActivities = None)
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (contextAgents) [xAPI 2.0]") {
        val left: StatementContext = StatementContext(
          contextAgents = Some(
            List(
              ContextAgent(
                objectType = ContextAgent.contextType,
                agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
                relevantTypes = Some(
                  List(
                    IRI("https://lrs.integralla.io/types/instructor"),
                    IRI("https://lrs.integralla.io/types/subject-matter-expert")
                  )
                )
              ),
              ContextAgent(
                objectType = ContextAgent.contextType,
                agent = Agent(mbox = Some(MBox("mailto:other.agent@example.com")))
              )
            )
          )
        )
        val right: StatementContext = left.copy(
          contextAgents = Some(
            left.contextAgents.get.take(1)
          )
        )
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (contextGroups) [xAPI 2.0]") {
        val left: StatementContext = StatementContext(
          contextGroups = Some(
            List(
              ContextGroup(
                objectType = ContextGroup.contextType,
                group = Group(
                  objectType = StatementObjectType.Group,
                  name = Some("Identified Group"),
                  mbox = Some(MBox("mailto:identified.group@integralla.io"))
                ),
                relevantTypes = Some(
                  List(
                    IRI("https://lrs.integralla.io/types/team"),
                    IRI("https://lrs.integralla.io/types/leads")
                  )
                )
              ),
              ContextGroup(
                objectType = ContextGroup.contextType,
                group = Group(
                  objectType = StatementObjectType.Group,
                  mbox = Some(MBox("mailto:other.group@integralla.io"))
                )
              )
            )
          )
        )
        val right: StatementContext = left.copy(
          contextGroups = Some(
            left.contextGroups.get.take(1)
          )
        )
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (revision)") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy(revision = None)
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (platform)") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy(platform = None)
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (language)") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy(language = None)
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (statement)") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext =
          sampleContext.copy(statement =
            Some(StatementRef(StatementObjectType.StatementRef, UUID.randomUUID()))
          )
        assert(left.isEquivalentTo(right) === false)
      }
      it("should return false if the objects are not equivalent (extensions)") {
        val left: StatementContext = sampleContext.copy()
        val right: StatementContext = sampleContext.copy(extensions = None)
        assert(left.isEquivalentTo(right) === false)
      }
    }

    describe("agentReferences") {
      it("should return a list of agent references for an instructor") {
        val context: StatementContext = StatementContext(
          instructor = Some(
            Agent(
              objectType = Some(StatementObjectType.Agent),
              mbox = Some(MBox("mailto:instructor@integralla.io"))
            )
          )
        )
        val references: List[AgentReference] = context.agentReferences(inSubStatement = false)
        assert(references.length === 1)
        assert(references.head.referenceType === InstructorRef)
        assert(references.head.inSubStatement === false)
        assert(references.head.asGroupMember === false)
      }
      it("should return a list of agent references for an team") {
        val context: StatementContext = StatementContext(
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
          )
        )
        val references: List[AgentReference] = context.agentReferences(inSubStatement = false)
        assert(references.length === 3)
        assert(references.forall(_.inSubStatement === false))
        assert(references.forall(_.referenceType === TeamRef))
        assert(references.count(!_.asGroupMember) === 1)
        assert(references.count(_.asGroupMember) === 2)
      }
      it("should return a list of references for context agents [xAPI 2.0]") {
        val context: StatementContext = StatementContext(
          contextAgents = Some(
            List(
              ContextAgent(
                objectType = ContextAgent.contextType,
                agent = Agent(mbox = Some(MBox("mailto:context.agent.a@example.com")))
              ),
              ContextAgent(
                objectType = ContextAgent.contextType,
                agent = Agent(mbox = Some(MBox("mailto:context.agent.b@example.com")))
              )
            )
          )
        )
        val references: List[AgentReference] = context.agentReferences(inSubStatement = false)
        assert(references.length === 2)
        assert(references.forall(_.referenceType === ContextAgentRef))
        assert(references.forall(_.inSubStatement === false))
        assert(references.forall(_.asGroupMember === false))
      }
      it("should return a list of references for context groups [xAPI 2.0]") {
        val context: StatementContext = StatementContext(
          contextGroups = Some(
            List(
              ContextGroup(
                objectType = ContextGroup.contextType,
                group = Group(
                  objectType = StatementObjectType.Group,
                  name = Some("Identified Group"),
                  mbox = Some(MBox("mailto:identified.group@integralla.io")),
                  member = Some(
                    List(
                      Agent(mbox = Some(MBox("mailto:member.one@example.com"))),
                      Agent(mbox = Some(MBox("mailto:member.two@example.com")))
                    )
                  )
                )
              ),
              ContextGroup(
                objectType = ContextGroup.contextType,
                group = Group(
                  objectType = StatementObjectType.Group,
                  mbox = Some(MBox("mailto:other.group@integralla.io"))
                )
              )
            )
          )
        )
        val references: List[AgentReference] = context.agentReferences(inSubStatement = false)
        assert(references.length === 4)
        assert(references.forall(_.inSubStatement === false))
        assert(references.forall(_.referenceType === ContextGroupRef))
        assert(references.count(_.asGroupMember) === 2)
        assert(references.count(_.asGroupMember) === 2)
      }
      it("should return references that reflect if the context is in a sub-statement") {
        val context: StatementContext = StatementContext(
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
          )
        )
        val references: List[AgentReference] = context.agentReferences(inSubStatement = true)
        assert(references.length === 3)
        assert(references.forall(_.inSubStatement === true))
      }
      it("should return an empty list if the context does not contain any agent references") {
        val context: StatementContext = StatementContext(
          registration = Some(UUID.randomUUID())
        )
        val references: List[AgentReference] = context.agentReferences(inSubStatement = false)
        assert(references.isEmpty)
      }
    }
  }
}
