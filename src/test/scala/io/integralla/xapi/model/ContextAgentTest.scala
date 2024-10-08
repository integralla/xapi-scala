package io.integralla.xapi.model

import io.integralla.xapi.model.exceptions.StatementValidationException
import io.integralla.xapi.model.references.{AgentReference, ContextAgentRef}
import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

class ContextAgentTest extends AnyFunSpec {
  describe("ContextAgent") {
    describe("[encoding/decoding]") {
      it("should encode/decode a context agent object") {
        val contextAgent: ContextAgent = ContextAgent(
          objectType = ContextAgent.contextType,
          agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/instructor"),
              IRI("https://lrs.integralla.io/types/subject-matter-expert")
            )
          )
        )

        val encoded: String = contextAgent.toJson()
        assert(
          encoded === """{"objectType":"contextAgent","agent":{"mbox":"mailto:context.agent@example.com"},"relevantTypes":["https://lrs.integralla.io/types/instructor","https://lrs.integralla.io/types/subject-matter-expert"]}""".stripMargin
        )

        val decoded: Try[ContextAgent] = ContextAgent(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === contextAgent)
      }
      it("should encode/decode a context agent object without any relevant types") {
        val contextAgent: ContextAgent = ContextAgent(
          objectType = ContextAgent.contextType,
          agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
          relevantTypes = None
        )

        val encoded: String = contextAgent.toJson()
        assert(
          encoded === """{"objectType":"contextAgent","agent":{"mbox":"mailto:context.agent@example.com"}}""".stripMargin
        )

        val decoded: Try[ContextAgent] = ContextAgent(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === contextAgent)
      }
    }

    describe("[equivalence]") {
      it("should return true if both objects are equivalent (no relevant types)") {
        val left: ContextAgent = ContextAgent(
          objectType = ContextAgent.contextType,
          agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
          relevantTypes = None
        )
        val right: ContextAgent = left.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both objects are equivalent (with relevant types)") {
        val left: ContextAgent = ContextAgent(
          objectType = ContextAgent.contextType,
          agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/instructor"),
              IRI("https://lrs.integralla.io/types/subject-matter-expert")
            )
          )
        )
        val right: ContextAgent = left.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return true if both objects are equivalent excepting relevant types list order") {
        val left: ContextAgent = ContextAgent(
          objectType = ContextAgent.contextType,
          agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/instructor"),
              IRI("https://lrs.integralla.io/types/subject-matter-expert")
            )
          )
        )
        val right: ContextAgent = left.copy(
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/subject-matter-expert"),
              IRI("https://lrs.integralla.io/types/instructor")
            )
          )
        )
        assert(left.isEquivalentTo(right))
      }
      it("should return false if both objects are not equivalent") {
        val left: ContextAgent = ContextAgent(
          objectType = ContextAgent.contextType,
          agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/instructor"),
              IRI("https://lrs.integralla.io/types/subject-matter-expert")
            )
          )
        )
        val right: ContextAgent = left.copy(
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/instructor")
            )
          )
        )
        assert(left.isEquivalentTo(right) === false)
      }
    }

    describe("[validation]") {
      it("should throw a validation error if the object type value is not recognized") {
        val exception = intercept[StatementValidationException] {
          ContextAgent(
            objectType = "toto",
            agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
            relevantTypes = None
          )
        }
        assert(
          exception.getMessage.contains("Incorrect objectType value for a context agent object")
        )
      }
      it("should throw a validation error if the relevantTypes list is empty") {
        val exception = intercept[StatementValidationException] {
          ContextAgent(
            objectType = ContextAgent.contextType,
            agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
            relevantTypes = Some(List.empty[IRI])
          )
        }
        assert(exception.getMessage.contains("The relevantTypes list cannot be empty"))
      }
    }

    describe("agentReference") {
      it("should return an agent reference for the agent") {
        val contextAgent: ContextAgent = ContextAgent(
          objectType = ContextAgent.contextType,
          agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/instructor"),
              IRI("https://lrs.integralla.io/types/subject-matter-expert")
            )
          )
        )
        val reference: AgentReference = contextAgent.agentReference(false)
        assert(reference.agent === contextAgent.agent)
        assert(reference.referenceType === ContextAgentRef)
        assert(reference.inSubStatement === false)
        assert(reference.asGroupMember === false)
      }
      it("should return a reference that reflect if the context is in a sub-statement") {
        val contextAgent: ContextAgent = ContextAgent(
          objectType = ContextAgent.contextType,
          agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/instructor"),
              IRI("https://lrs.integralla.io/types/subject-matter-expert")
            )
          )
        )
        val reference: AgentReference = contextAgent.agentReference(true)
        assert(reference.agent === contextAgent.agent)
        assert(reference.referenceType === ContextAgentRef)
        assert(reference.inSubStatement === true)
        assert(reference.asGroupMember === false)
      }
    }
  }
}
