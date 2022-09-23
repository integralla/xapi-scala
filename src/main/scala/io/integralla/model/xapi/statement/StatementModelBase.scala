package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}

trait StatementModelBase {
  type T

  implicit val decoder: Decoder[T]
  implicit val encoder: Encoder[T]

}

