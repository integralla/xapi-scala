package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.common.Equivalence
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType

import java.util.UUID

/** A pointer to another pre-existing statement
  *
  * @param objectType A statement object type
  * @param id         The UUID of the referenced statement
  */
case class StatementRef(objectType: StatementObjectType, id: UUID) extends Equivalence {

  /** Generates a signature that can be used to test logical equivalence between objects
    *
    * @return A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      combine {
        List(objectType.toString, id.toString)
      }
    }
  }
}

object StatementRef {
  implicit val decoder: Decoder[StatementRef] = deriveDecoder[StatementRef]
  implicit val encoder: Encoder[StatementRef] = deriveEncoder[StatementRef]
}
