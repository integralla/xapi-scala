package io.integralla.xapi.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.xapi.model.common.{Decodable, Encodable}

/** Statement query results model
  *
  * @param statements
  *   List of statements
  * @param more
  *   Relative IRL that can be used to fetch more results, including the full
  *   path and optionally a query string but excluding scheme, host, and port.
  */
case class StatementQueryResult(statements: StatementList, more: Option[String] = None)
    extends Encodable[StatementQueryResult]

object StatementQueryResult extends Decodable[StatementQueryResult] {
  implicit val decoder: Decoder[StatementQueryResult] = deriveDecoder[StatementQueryResult]
  implicit val encoder: Encoder[StatementQueryResult] =
    deriveEncoder[StatementQueryResult].mapJson(_.dropNullValues)
}
