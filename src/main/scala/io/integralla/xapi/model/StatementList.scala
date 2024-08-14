package io.integralla.xapi.model

import io.circe.{Decoder, Encoder}
import io.integralla.xapi.model.common.{Decodable, Encodable}

/** Represents a list of statements
  *
  * @param statements
  *   List of statements
  */
case class StatementList(statements: List[Statement]) extends Encodable[StatementList]

object StatementList extends Decodable[StatementList] {
  implicit val encoder: Encoder[StatementList] =
    Encoder.encodeList[Statement].contramap[StatementList](_.statements)
  implicit val decoder: Decoder[StatementList] =
    Decoder.decodeList[Statement].map[StatementList](StatementList.apply)
}
