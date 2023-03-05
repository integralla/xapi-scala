package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.time4j.{ClockUnit, Duration}

import java.text.ParseException

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
  extensions: Option[ExtensionMap]
) extends StatementValidation with Equivalence {
  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateDuration
    )
  }

  private def validateDuration: Either[String, Boolean] = {
    duration
      .map(duration => {
        try {
          val _ = Duration.parsePeriod(duration)
          Right(true)
        } catch {
          case e: ParseException =>
            Left(
              s"The supplied duration could not be parsed: duration($duration), errorIndex(${e.getErrorOffset})"
            )
          case _: Throwable => Left(s"An error occurred parsing the duration")
        }
      }).getOrElse(Right(true))
  }

  /** Generates a signature that can be used to test logical equivalence between objects
    *
    * Per the specification, when comparing durations, any precision
    * beyond 0.01 second precision SHOULD* NOT be included in the
    * comparison. To accommodate this requirement, we extract the
    * nano-seconds from the duration and round it to the nearest
    * ten-millionth
    *
    * @return A string identifier
    */
  override protected[statement] def signature(): String = {

    def truncateDuration(duration: String): String = {
      val d = Duration.parsePeriod(duration)
      val nanos = d.getPartialAmount(ClockUnit.NANOS)
      val excess = nanos - ((nanos / 10e6).floor * 10e6).toInt
      d.plus(-excess, ClockUnit.NANOS).toString
    }

    hash {
      combine {
        List(
          score.map(_.signature()).getOrElse(placeholder),
          success.map(_.toString).getOrElse(placeholder),
          completion.map(_.toString).getOrElse(placeholder),
          response.getOrElse(placeholder),
          duration.map(truncateDuration).getOrElse(placeholder),
          extensions.map(_.signature()).getOrElse(placeholder)
        )
      }
    }
  }
}

object StatementResult {
  implicit val decoder: Decoder[StatementResult] = deriveDecoder[StatementResult]
  implicit val encoder: Encoder[StatementResult] = deriveEncoder[StatementResult].mapJson(_.dropNullValues)
}
