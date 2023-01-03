package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

/** A score represents the outcome of a graded Activity achieved by an Agent
  *
  * @param scaled The score related to the experience as modified by scaling and/or normalization. Decimal number between -1 and 1, inclusive
  * @param raw    The score achieved by the Actor in the experience described by the Statement. This is not modified by any scaling or normalization. Decimal number between min and max (if present, otherwise unrestricted), inclusive
  * @param min    The lowest possible score for the experience described by the Statement. Decimal number less than max (if present)
  * @param max    The highest possible score for the experience described by the Statement. Decimal number greater than min (if present)
  */
case class Score(
  scaled: Option[Double],
  raw: Option[Double],
  min: Option[Double],
  max: Option[Double]
) extends StatementValidation {
  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateScaled,
      validateRawIsNotLessThanMin,
      validateRawIsNotGreaterThanMax,
      validateScoreBounds
    )
  }

  private def validateScaled: Either[String, Boolean] = {
    scaled
      .map((score: Double) => {
        if (score < -1 || score > 1) {
          Left("A scaled score must be a normalized value between -1 and 1, inclusive")
        } else {
          Right(true)
        }
      }).getOrElse(Right(true))
  }

  private def validateRawIsNotLessThanMin: Either[String, Boolean] = {
    raw
      .map((rawScore: Double) => {
        min
          .map((minScore: Double) => {
            if (rawScore < minScore) {
              Left("The raw score cannot be less than the lowest possible (min) score defined for the experience")
            } else {
              Right(true)
            }
          }).getOrElse(Right(true))
      }).getOrElse(Right(true))
  }

  private def validateRawIsNotGreaterThanMax: Either[String, Boolean] = {
    raw
      .map((rawScore: Double) => {
        max
          .map((maxScore: Double) => {
            if (rawScore > maxScore) {
              Left("The raw score cannot be greater than the highest possible (max) score defined for the experience")
            } else {
              Right(true)
            }
          }).getOrElse(Right(true))
      }).getOrElse(Right(true))
  }

  private def validateScoreBounds: Either[String, Boolean] = {
    max
      .map((maxScore: Double) => {
        min
          .map((minScore: Double) => {
            if (maxScore <= minScore) {
              Left("The highest possible score (max) must be greater than the lowest possible score (min)")
            } else {
              Right(true)
            }
          }).getOrElse(Right(true))
      }).getOrElse(Right(true))
  }
}

object Score {
  implicit val decoder: Decoder[Score] = deriveDecoder[Score]
  implicit val encoder: Encoder[Score] = deriveEncoder[Score].mapJson(_.dropNullValues)
}
