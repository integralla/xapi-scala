package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType

import java.util.UUID

/**
 * A pointer to another pre-existing statement
 *
 * @param objectType A statement object type
 * @param id         The UUID of the referenced statement
 */
case class StatementRef(objectType: StatementObjectType, id: UUID)

object StatementRef extends StatementModelBase {
  override type T = StatementRef
  override implicit val decoder: Decoder[StatementRef] = deriveDecoder[StatementRef]
  override implicit val encoder: Encoder[StatementRef] = deriveEncoder[StatementRef]
}
