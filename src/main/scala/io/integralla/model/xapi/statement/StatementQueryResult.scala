package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class StatementQueryResult(statements: StatementList, more: Option[String] = None)

object StatementQueryResult {
  implicit val decoder: Decoder[StatementQueryResult] = deriveDecoder[StatementQueryResult]
  implicit val encoder: Encoder[StatementQueryResult] =
    deriveEncoder[StatementQueryResult].mapJson(_.dropNullValues)
}
