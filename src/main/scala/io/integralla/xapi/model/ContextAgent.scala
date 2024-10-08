package io.integralla.xapi.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import ContextAgent.contextType
import io.integralla.xapi.model.common.{Decodable, Encodable, Equivalence}
import io.integralla.xapi.model.references.{AgentReference, ContextAgentRef}

/** xAPI 2.0 Context Agent model
  *
  * @param objectType
  *   Static value that must be set to "contextAgent"
  * @param agent
  *   Agent object
  * @param relevantTypes
  *   List of "Relevant Type" IRIs
  */
case class ContextAgent(
  objectType: String,
  agent: Agent,
  relevantTypes: Option[List[IRI]] = None
) extends Encodable[ContextAgent] with Equivalence with StatementValidation {

  /** Return an agent reference for the context agent
    *
    * @param inSubStatement
    *   Whether the reference occurs in a sub-statement
    * @return
    *   Agent references
    */
  def agentReference(inSubStatement: Boolean): AgentReference = {
    AgentReference(
      agent = agent,
      referenceType = ContextAgentRef,
      inSubStatement = inSubStatement,
      asGroupMember = false
    )
  }

  override protected[xapi] def signature(): String = {
    hash {
      combine {
        List(
          objectType,
          agent.signature(),
          relevantTypes.map(_.map(_.signature()).sorted.mkString(separator)).getOrElse(placeholder)
        )
      }
    }
  }

  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateObjectType,
      validateRelevantTypes
    )
  }

  /** @return
    *   True if the objectType value is valid, else an description of the
    *   exception
    */
  private def validateObjectType: Either[String, Boolean] = {
    if (objectType != contextType) {
      Left("Incorrect objectType value for a context agent object")
    } else {
      Right(true)
    }
  }

  /** @return
    *   True if the relevantTypes property is undefined or non-empty, else a
    *   description of the exception
    */
  private def validateRelevantTypes: Either[String, Boolean] = {
    relevantTypes match {
      case Some(types) =>
        if (types.nonEmpty) Right(true) else Left("The relevantTypes list cannot be empty")
      case None => Right(true)
    }
  }
}

object ContextAgent extends Decodable[ContextAgent] {

  /** Required value for the objectType property */
  val contextType: String = "contextAgent"

  /** Implicit encoder/decoder instances */
  implicit val decoder: Decoder[ContextAgent] = deriveDecoder[ContextAgent]
  implicit val encoder: Encoder[ContextAgent] =
    deriveEncoder[ContextAgent].mapJson(_.dropNullValues)
}
