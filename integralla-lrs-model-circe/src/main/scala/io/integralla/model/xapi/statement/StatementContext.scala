package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.util.UUID

/** Provides a place to add contextual information to a statement
  *
  * @param registration      The registration that the statement is associated with
  * @param instructor        The instructor that the statement relates to, if not included as the actor of the statement
  * @param team              The team (group) that this statement relates to, if not included as the actor of the statement
  * @param contextActivities A map of the types of learning activity context that this statement is related to
  * @param revision          The revision of the learning activity associated with this statement
  * @param platform          The platform used in the experience of this learning activity
  * @param language          An RFC 5646 language tag representing the language in which the experience being recorded in this statement (mainly) occurred in, if applicable and known
  * @param statement         A reference to another statement to be considered as context for this statement
  * @param extensions        A map of any other domain-specific context relevant to this statement
  */
case class StatementContext(
  registration: Option[UUID],
  instructor: Option[StatementActor],
  team: Option[Group],
  contextActivities: Option[ContextActivities],
  revision: Option[String],
  platform: Option[String],
  language: Option[String],
  statement: Option[StatementRef],
  extensions: Option[Extensions]
)

object StatementContext {
  implicit val decoder: Decoder[StatementContext] = deriveDecoder[StatementContext]
  implicit val encoder: Encoder[StatementContext] = deriveEncoder[StatementContext].mapJson(_.dropNullValues)
}
