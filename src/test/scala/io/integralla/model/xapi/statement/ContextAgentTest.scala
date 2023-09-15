package io.integralla.model.xapi.statement

import io.integralla.model.utils.LRSModelUtils
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import scala.util.Try

class ContextAgentTest extends UnitSpec {
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

        val encoded: String = LRSModelUtils.toJSON[ContextAgent](contextAgent)
        assert(
          encoded === """{"objectType":"contextAgent","agent":{"mbox":"mailto:context.agent@example.com"},"relevantTypes":["https://lrs.integralla.io/types/instructor","https://lrs.integralla.io/types/subject-matter-expert"]}""".stripMargin
        )

        val decoded: Try[ContextAgent] = LRSModelUtils.fromJSON[ContextAgent](encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === contextAgent)
      }
      it("should encode/decode a context agent object without any relevant types") {
        val contextAgent: ContextAgent = ContextAgent(
          objectType = ContextAgent.contextType,
          agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
          relevantTypes = None
        )

        val encoded: String = LRSModelUtils.toJSON[ContextAgent](contextAgent)
        assert(
          encoded === """{"objectType":"contextAgent","agent":{"mbox":"mailto:context.agent@example.com"}}""".stripMargin
        )

        val decoded: Try[ContextAgent] = LRSModelUtils.fromJSON[ContextAgent](encoded)
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
        assertThrows[StatementValidationException] {
          ContextAgent(
            objectType = "toto",
            agent = Agent(mbox = Some(MBox("mailto:context.agent@example.com"))),
            relevantTypes = None
          )
        }
      }
    }
  }
}
