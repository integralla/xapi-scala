package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}

/**
 * Enumeration of supported interaction types
 */
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

  implicit val decoder: Decoder[InteractionType.Value] = Decoder.decodeEnumeration(InteractionType)
  implicit val encoder: Encoder[InteractionType.Value] = Encoder.encodeEnumeration(InteractionType)
}
