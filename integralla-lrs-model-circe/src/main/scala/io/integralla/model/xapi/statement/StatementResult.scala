package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.Duration
import java.time.format.DateTimeParseException

/** A Result represents a measured outcome related to the statement in which it is included
  *
  * @param score      The score of the agent in relation to the success or quality of the experience
  * @param success    Indicates whether or not the attempt on the activity was successful
  * @param completion Indicates whether or not the activity was completed
  * @param response   A response appropriately formatted for the given activity
  * @param duration   Period of time over which the statement occurred.
  * @param extensions A map of other properties as needed
  */
case class StatementResult(
  score: Option[Score],
  success: Option[Boolean],
  completion: Option[Boolean],
  response: Option[String],
  duration: Option[String],
  extensions: Option[Extensions]
) extends StatementValidation {
  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateDuration
    )
  }

  private def validateDuration: Either[String, Boolean] = {
    duration
      .map(duration => {
        try {
          val _ = Duration.parse(duration)
          Right(true)
        } catch {
          case e: DateTimeParseException =>
            Left(
              s"The supplied duration could not be parsed: duration(${e.getParsedString}), errorIndex(${e.getErrorIndex})"
            )
          case _: Throwable => Left(s"An error occurred parsing the duration")
        }
      }).getOrElse(Right(true))
  }
}

object StatementResult {
  implicit val decoder: Decoder[StatementResult] = deriveDecoder[StatementResult]
  implicit val encoder: Encoder[StatementResult] = deriveEncoder[StatementResult].mapJson(_.dropNullValues)
}
