package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}

/** Represents a list of statements
  *
  * @param statements
  *   List of statements
  */
case class StatementList(statements: List[Statement])

object StatementList {
  implicit val encoder: Encoder[StatementList] =
    Encoder.encodeList[Statement].contramap[StatementList](_.statements)
  implicit val decoder: Decoder[StatementList] =
    Decoder.decodeList[Statement].map[StatementList](StatementList.apply)
}
