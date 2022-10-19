package io.integralla.model.xapi.statement

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}

import scala.util.{Failure, Success, Try}

/**
 * An enumeration of statement object types
 */
object StatementObjectType extends Enumeration {
  type StatementObjectType = Value
  val Activity, Agent, Group, StatementRef, SubStatement = Value

  implicit val decoder: Decoder[StatementObjectType.Value] = (c: HCursor) => Decoder.decodeString(c).flatMap { str =>
    Try(StatementObjectType.withName(str)) match {
      case Success(a) => Right(a)
      case Failure(_) =>
        Left(
          DecodingFailure(
            s"Couldn't decode value '$str'. " +
              s"Allowed values: '${StatementObjectType.values.mkString(",")}'",
            c.history
          )
        )
    }
  }
  implicit val encoder: Encoder[StatementObjectType.Value] = (a: StatementObjectType.Value) => {
    Encoder.encodeString(a.toString)
  }
}
