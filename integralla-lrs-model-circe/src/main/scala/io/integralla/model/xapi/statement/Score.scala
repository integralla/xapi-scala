package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.exceptions.StatementValidationException

/**
 * A score represents the outcome of a graded Activity achieved by an Agent
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
) extends StatementModelValidation {
  override def validate(): Unit = {
    validateScaled()
    validateRaw()
    validateScoreBounds()
  }

  def validateScaled(): Unit = {
    scaled.foreach((score: Double) => {
      if (score < -1 || score > 1) {
        throw new StatementValidationException("A scaled score must be a normalized value between -1 and 1, inclusive")
      }
    })
  }

  def validateRaw(): Unit = {
    raw.foreach((rawScore: Double) => {
      min.foreach((minScore: Double) => {
        if (rawScore < minScore) {
          throw new StatementValidationException("The raw score cannot be less than the lowest possible (min) score defined for the experience")
        }
      })

      max.foreach((maxScore: Double) => {
        if (rawScore > maxScore) {
          throw new StatementValidationException("The raw score cannot be greater than the highest possible (max) score defined for the experience")
        }
      })
    })
  }

  def validateScoreBounds(): Unit = {
    max.foreach((maxScore: Double) => {
      min.foreach((minScore: Double) => {
        if (maxScore <= minScore) {
          throw new StatementValidationException("The highest possible score (max) must be greater than the lowest possible score (min)")
        }
      })
    })
  }
}

object Score extends StatementModelBase {
  override type T = Score
  override implicit val decoder: Decoder[Score] = deriveDecoder[Score]
  override implicit val encoder: Encoder[Score] = deriveEncoder[Score].mapJson(_.dropNullValues)
}
