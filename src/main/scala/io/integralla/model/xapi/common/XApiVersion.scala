package io.integralla.model.xapi.common

import io.circe.{Decoder, Encoder}
import io.integralla.model.utils.LRSModel
import io.integralla.model.xapi.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.StatementValidation

import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

case class XApiVersion(major: Int, minor: Int, patch: Option[Int]) extends LRSModel with StatementValidation {

  def format: String =
    (List(major, minor) ++ patch).mkString(".")

  override def validate: Seq[Either[String, Boolean]] =
    Seq(
      Try(
        major match
          case 1 =>
            minor match
              case 0 =>
                if (patch.getOrElse(0) <= 3) true else throw new Exception()
              case _ => throw new Exception()
          case 2 =>
            minor match
              case 0 =>
                if (patch.getOrElse(0) == 0) true else throw new Exception()
              case _ => throw new Exception()
          case _ => throw new Exception()
      ) match
        case Success(_) => Right(true)
        case Failure(_) => Left("Unsupported xAPI version")
    )
}

object XApiVersion {

  /** Latest patch version supported for xAPI 1.0 */
  val XAPI_V1: String = "1.0.3"

  /** Latest patch version supported for xAPI 2.0 */
  val XAPI_V2: String = "2.0.0"

  /** List of latest minor and patch versions supported, for each major version
    */
  val supportedVersions: List[String] = List(XAPI_V1, XAPI_V2)

  implicit val encoder: Encoder[XApiVersion] = Encoder.encodeString.contramap[XApiVersion](_.format)

  implicit val decoder: Decoder[XApiVersion] = Decoder.decodeString.map[XApiVersion](value =>
    val pattern: Regex = """^(0|[1-9]\d*)\.(0|[1-9]\d*)(\.(0|[1-9]\d*))?$""".r
    value match
      case pattern(major, minor, _, patch) => XApiVersion(major.toInt, minor.toInt, Option(patch).map(_.toInt))
      case _                               => throw new StatementValidationException("Invalid xAPI version")
  )
}
