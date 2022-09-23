package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}

/**
 * An enumeration of statement object types
 */
object StatementObjectType extends Enumeration {
  type StatementObjectType = Value
  val Activity, Agent, Group, StatementRef, SubStatement = Value

  implicit val decoder: Decoder[StatementObjectType.Value] = Decoder.decodeEnumeration(StatementObjectType)
  implicit val encoder: Encoder[StatementObjectType.Value] = Encoder.encodeEnumeration(StatementObjectType)
}
