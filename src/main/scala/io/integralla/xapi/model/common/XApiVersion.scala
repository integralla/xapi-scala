package io.integralla.xapi.model.common

import io.circe.{Decoder, Encoder}
import io.integralla.xapi.model.exceptions.StatementValidationException
import io.integralla.xapi.model.statement.StatementValidation

import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

/** xAPI version model
  *
  * @param major
  *   Major version number
  * @param minor
  *   Minor version number
  * @param patch
  *   Patch version number
  */
case class XApiVersion(major: Int, minor: Int, patch: Option[Int])
    extends Encodable[XApiVersion] with StatementValidation {

  /** @return Version formatted as a string */
  def format: String =
    (List(major, minor) ++ patch).mkString(".")

  /** Validate that the version is a valid xAPI version
    *
    * @return
    *   Sequence whose values are an `Either` where `Left` provides a
    *   description of validation exception and `Right` represents a boolean
    *   indicating that validation succeeded
    */
  override def validate: Seq[Either[String, Boolean]] =
    Seq(
      Try(
        major match {
          case 1 =>
            minor match {
              case 0 =>
                if (patch.getOrElse(0) <= 9) true else throw new Exception()
              case _ => throw new Exception()
            }
          case 2 =>
            minor match {
              case 0 =>
                if (patch.getOrElse(0) == 0) true else throw new Exception()
              case _ => throw new Exception()
            }
          case _ => throw new Exception()
        }
      ) match {
        case Success(_) => Right(true)
        case Failure(_) => Left("Unsupported xAPI version")
      }
    )
}

object XApiVersion extends Decodable[XApiVersion] {

  /** Latest patch version supported for xAPI 1.0 */
  val XAPI_V1: String = "1.0.3"

  /** Latest patch version supported for xAPI 2.0 */
  val XAPI_V2: String = "2.0.0"

  /** List of latest minor and patch versions supported, for each major version
    */
  val supportedVersions: List[String] = List(XAPI_V1, XAPI_V2)

  implicit val encoder: Encoder[XApiVersion] = Encoder.encodeString.contramap[XApiVersion](_.format)

  implicit val decoder: Decoder[XApiVersion] = Decoder.decodeString.map[XApiVersion](value => {
    val pattern: Regex = """^(0|[1-9]\d*)\.(0|[1-9]\d*)(\.(0|[1-9]\d*))?$""".r
    value match {
      case pattern(major, minor, _, patch) =>
        XApiVersion(major.toInt, minor.toInt, Option(patch).map(_.toInt))
      case _ => throw new StatementValidationException("Invalid xAPI version")
    }
  })
}
