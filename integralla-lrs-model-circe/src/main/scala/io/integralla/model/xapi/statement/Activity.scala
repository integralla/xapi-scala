package io.integralla.model.xapi.statement

import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType
import io.integralla.model.xapi.statement.identifiers.IRI

/**
 * A statement activity
 *
 * @param objectType Activity statement object tyope
 * @param id         An identifier for a single unique activity
 * @param definition An activity definition
 */
case class Activity(
  objectType: Option[StatementObjectType],
  id: IRI,
  definition: Option[ActivityDefinition])

object Activity {
  implicit val decoder: Decoder[Activity] = deriveDecoder[Activity]
  implicit val encoder: Encoder[Activity] = deriveEncoder[Activity].mapJson(_.dropNullValues)
}
