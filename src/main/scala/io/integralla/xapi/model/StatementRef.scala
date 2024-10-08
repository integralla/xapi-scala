package io.integralla.xapi.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.xapi.model.StatementObjectType.StatementObjectType
import io.integralla.xapi.model.common.{Decodable, Encodable, Equivalence}

import java.util.UUID

/** A pointer to another pre-existing statement
  *
  * @param objectType
  *   A statement object type
  * @param id
  *   The UUID of the referenced statement
  */
case class StatementRef(objectType: StatementObjectType, id: UUID)
    extends Encodable[StatementRef] with Equivalence {

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      combine {
        List(objectType.toString, id.toString)
      }
    }
  }
}

object StatementRef extends Decodable[StatementRef] {
  implicit val decoder: Decoder[StatementRef] = deriveDecoder[StatementRef]
  implicit val encoder: Encoder[StatementRef] = deriveEncoder[StatementRef]
}
