package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.model.xapi.common.Equivalence
import io.integralla.model.xapi.statement.identifiers.IRI
import io.integralla.model.xapi.statement.ContextGroup.contextType

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
    Seq(validateObjectType)
  }

  /** @return
    *   True if the objectType value is valid, else an description of the
    *   exception
    */
  private def validateObjectType: Either[String, Boolean] = {
    if (objectType != contextType) {
      Left("Incorrect object type value for a context group object")
    } else {
      Right(true)
    }
  }
}

object ContextGroup {

  /** Required value for the objectType property */
  val contextType: String = "contextGroup"

  /** Implicit encoder/decoder instances */
  implicit val decoder: Decoder[ContextGroup] = deriveDecoder[ContextGroup]
  implicit val encoder: Encoder[ContextGroup] = deriveEncoder[ContextGroup].mapJson(_.dropNullValues)
}
