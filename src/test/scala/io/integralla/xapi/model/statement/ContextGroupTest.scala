package io.integralla.xapi.model.statement

import io.integralla.xapi.model.exceptions.StatementValidationException
import io.integralla.xapi.model.references.{AgentReference, ContextGroupRef}
import io.integralla.xapi.model.utils.LRSModelUtils
import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

class ContextGroupTest extends AnyFunSpec {
  describe("ContextGroup") {
    describe("[encoding/decoding]") {
      it("should encode/decode a context group object") {
        val contextGroup: ContextGroup = ContextGroup(
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

        val encoded: String = LRSModelUtils.toJSON[ContextGroup](contextGroup, spaces = true)
        val expected: String =
          """{
            |  "objectType" : "contextGroup",
            |  "group" : {
            |    "objectType" : "Group",
            |    "name" : "Identified Group",
            |    "mbox" : "mailto:identified.group@integralla.io",
            |    "member" : [
            |      {
            |        "mbox" : "mailto:member.one@example.com"
            |      },
            |      {
            |        "mbox" : "mailto:member.two@example.com"
            |      }
            |    ]
            |  },
            |  "relevantTypes" : [
            |    "https://lrs.integralla.io/types/team",
            |    "https://lrs.integralla.io/types/leads"
            |  ]
            |}""".stripMargin

        assert(encoded === expected)

        val decoded: Try[ContextGroup] = LRSModelUtils.fromJSON[ContextGroup](encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === contextGroup)
      }
      it("should encode/decode a context group object without any relevant types") {
        val contextGroup: ContextGroup = ContextGroup(
          objectType = ContextGroup.contextType,
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Anonymous Group"),
            member = Some(
              List(
                Agent(mbox = Some(MBox("mailto:member.one@example.com"))),
                Agent(mbox = Some(MBox("mailto:member.two@example.com")))
              )
            )
          ),
          relevantTypes = None
        )

        val encoded: String = LRSModelUtils.toJSON[ContextGroup](contextGroup, spaces = true)
        val expected: String =
          """{
            |  "objectType" : "contextGroup",
            |  "group" : {
            |    "objectType" : "Group",
            |    "name" : "Anonymous Group",
            |    "member" : [
            |      {
            |        "mbox" : "mailto:member.one@example.com"
            |      },
            |      {
            |        "mbox" : "mailto:member.two@example.com"
            |      }
            |    ]
            |  }
            |}""".stripMargin

        assert(encoded === expected)

        val decoded: Try[ContextGroup] = LRSModelUtils.fromJSON[ContextGroup](encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === contextGroup)
      }
    }

    describe("[equivalence]") {
      it("should return true if both objects are equivalent (no relevant types)") {
        val left: ContextGroup = ContextGroup(
          objectType = ContextGroup.contextType,
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Anonymous Group"),
            member = Some(
              List(
                Agent(mbox = Some(MBox("mailto:member.one@example.com"))),
                Agent(mbox = Some(MBox("mailto:member.two@example.com")))
              )
            )
          ),
          relevantTypes = None
        )
        val right: ContextGroup = left.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both objects are equivalent (with relevant types)") {
        val left: ContextGroup = ContextGroup(
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
        val right: ContextGroup = left.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both objects are equivalent excepting relevant types list order") {
        val left: ContextGroup = ContextGroup(
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
        val right: ContextGroup = left.copy(
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/leads"),
              IRI("https://lrs.integralla.io/types/team")
            )
          )
        )
        assert(left.isEquivalentTo(right))
      }
      it("should return false if the objects are not equivalent") {
        val left: ContextGroup = ContextGroup(
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
        val right: ContextGroup = left.copy(
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/team")
            )
          )
        )
        assert(left.isEquivalentTo(right) === false)
      }
    }

    describe("[validation]") {
      it("should throw a validation error if the object type value is not recognized") {
        val exception = intercept[StatementValidationException] {
          ContextGroup(
            objectType = "tata",
            group = Group(
              objectType = StatementObjectType.Group,
              name = Some("Anonymous Group"),
              member = Some(
                List(
                  Agent(mbox = Some(MBox("mailto:member.one@example.com"))),
                  Agent(mbox = Some(MBox("mailto:member.two@example.com")))
                )
              )
            ),
            relevantTypes = None
          )
        }
        assert(exception.getMessage.contains("Incorrect objectType value for a context group object"))
      }
      it("should throw a validation error if the relevantTypes list is empty") {
        val exception = intercept[StatementValidationException] {
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
            relevantTypes = Some(List.empty[IRI])
          )
        }
        assert(exception.getMessage.contains("The relevantTypes list cannot be empty"))
      }
    }

    describe("agentReferences") {
      it("should return a reference for an identified group") {
        val context: ContextGroup = ContextGroup(
          objectType = ContextGroup.contextType,
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Identified Group"),
            mbox = Some(MBox("mailto:identified.group@integralla.io"))
          )
        )
        val references: List[AgentReference] = context.agentReferences(false)
        assert(references.length === 1)
        assert(references.head.agent === context.group)
        assert(references.head.referenceType === ContextGroupRef)
        assert(references.head.inSubStatement === false)
        assert(references.head.asGroupMember === false)
      }
      it("should return a reference for each group member") {
        val context: ContextGroup = ContextGroup(
          objectType = ContextGroup.contextType,
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Anonymous Group"),
            member = Some(
              List(
                Agent(mbox = Some(MBox("mailto:member.one@example.com"))),
                Agent(mbox = Some(MBox("mailto:member.two@example.com")))
              )
            )
          ),
          relevantTypes = None
        )
        val references: List[AgentReference] = context.agentReferences(false)
        assert(references.length === 2)
        assert(references.forall(_.referenceType === ContextGroupRef))
        assert(references.forall(_.inSubStatement === false))
        assert(references.forall(_.asGroupMember === true))
      }
      it("should return references that reflect if the context is in a sub-statement") {
        val context: ContextGroup = ContextGroup(
          objectType = ContextGroup.contextType,
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Identified Group"),
            mbox = Some(MBox("mailto:identified.group@integralla.io"))
          )
        )
        val references: List[AgentReference] = context.agentReferences(true)
        assert(references.length === 1)
        assert(references.head.agent === context.group)
        assert(references.head.referenceType === ContextGroupRef)
        assert(references.head.inSubStatement === true)
        assert(references.head.asGroupMember === false)
      }
    }
  }
}
