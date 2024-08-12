package io.integralla.xapi.model.statement

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import ContextGroup.contextType
import io.integralla.xapi.model.common.Equivalence
import io.integralla.xapi.model.references.{AgentReference, ContextGroupRef}

/** Model for xAPI 2.0 Context Group object
  *
  * @param objectType
  *   Static value that must be set to "contextGroup"
  * @param group
  *   Group object
  * @param relevantTypes
  *   List of "Relevant Type" IRIs
  */
case class ContextGroup(
  objectType: String,
  group: Group,
  relevantTypes: Option[List[IRI]] = None
) extends Equivalence with StatementValidation {

  /** A list of agent references composed of the those identified by group (if
    * identified) and all member agents (if any)
    *
    * @param inSubStatement
    *   Whether the reference occurs in a sub-statement
    * @return
    *   A list of agent references
    */
  def agentReferences(inSubStatement: Boolean): List[AgentReference] = {
    group
      .asList().map(agent =>
        AgentReference(
          agent = agent._1,
          referenceType = ContextGroupRef,
          inSubStatement = inSubStatement,
          asGroupMember = agent._2
        )
      )
  }

  override protected[xapi] def signature(): String = {
    hash {
      combine {
        List(
          objectType,
          group.signature(),
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
      Left("Incorrect objectType value for a context group object")
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

object ContextGroup {

  /** Required value for the objectType property */
  val contextType: String = "contextGroup"

  /** Implicit encoder/decoder instances */
  implicit val decoder: Decoder[ContextGroup] = deriveDecoder[ContextGroup]
  implicit val encoder: Encoder[ContextGroup] =
    deriveEncoder[ContextGroup].mapJson(_.dropNullValues)
}
