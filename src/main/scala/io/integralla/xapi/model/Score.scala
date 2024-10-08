package io.integralla.xapi.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.xapi.model.common.{Decodable, Encodable, Equivalence}

/** A score represents the outcome of a graded Activity achieved by an Agent
  *
  * @param scaled
  *   The score related to the experience as modified by scaling and/or
  *   normalization. Decimal number between -1 and 1, inclusive
  * @param raw
  *   The score achieved by the Actor in the experience described by the
  *   Statement. This is not modified by any scaling or normalization. Decimal
  *   number between min and max (if present, otherwise unrestricted), inclusive
  * @param min
  *   The lowest possible score for the experience described by the Statement.
  *   Decimal number less than max (if present)
  * @param max
  *   The highest possible score for the experience described by the Statement.
  *   Decimal number greater than min (if present)
  */
case class Score(
  scaled: Option[Double] = None,
  raw: Option[Double] = None,
  min: Option[Double] = None,
  max: Option[Double] = None
) extends Encodable[Score] with Equivalence with StatementValidation {
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
              Left(
                "The raw score cannot be less than the lowest possible (min) score defined for the experience"
              )
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
              Left(
                "The raw score cannot be greater than the highest possible (max) score defined for the experience"
              )
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
              Left(
                "The highest possible score (max) must be greater than the lowest possible score (min)"
              )
            } else {
              Right(true)
            }
          }).getOrElse(Right(true))
      }).getOrElse(Right(true))
  }

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * The signature is composed of each property, cast as a string when defined,
    * or replaced with the default placeholder value when undefined
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {

    def format(double: Double): String = "%1.16f".format(double)
    hash {
      combine {
        List(
          scaled.map(format).getOrElse(placeholder),
          raw.map(format).getOrElse(placeholder),
          min.map(format).getOrElse(placeholder),
          max.map(format).getOrElse(placeholder)
        )
      }
    }
  }
}

object Score extends Decodable[Score] {
  implicit val decoder: Decoder[Score] = deriveDecoder[Score]
  implicit val encoder: Encoder[Score] = deriveEncoder[Score].mapJson(_.dropNullValues)
}
