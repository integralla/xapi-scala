package io.integralla.xapi.model

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}

import scala.util.{Failure, Success, Try}

/** Enumeration of supported interaction types */
object InteractionType extends Enumeration {
  type InteractionType = Value

  val CHOICE: InteractionType.Value = Value("choice")
  val FILL_IN: InteractionType.Value = Value("fill-in")
  val LIKERT: InteractionType.Value = Value("likert")
  val LONG_FILL_IN: InteractionType.Value = Value("long-fill-in")
  val MATCHING: InteractionType.Value = Value("matching")
  val NUMERIC: InteractionType.Value = Value("numeric")
  val OTHER: InteractionType.Value = Value("other")
  val PERFORMANCE: InteractionType.Value = Value("performance")
  val SEQUENCING: InteractionType.Value = Value("sequencing")
  val TRUE_FALSE: InteractionType.Value = Value("true-false")

  implicit val decoder: Decoder[InteractionType.Value] = (c: HCursor) =>
    Decoder.decodeString(c).flatMap { str =>
      Try(InteractionType.withName(str)) match {
        case Success(a) => Right(a)
        case Failure(_) =>
          Left(
            DecodingFailure(
              s"Couldn't decode value '$str'. " +
                s"Allowed values: '${InteractionType.values.mkString(",")}'",
              c.history
            )
          )
      }
    }
  implicit val encoder: Encoder[InteractionType.Value] = (a: InteractionType.Value) => {
    Encoder.encodeString(a.toString)
  }
}
