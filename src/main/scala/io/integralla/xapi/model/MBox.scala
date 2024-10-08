package io.integralla.xapi.model

import io.circe.{Decoder, Encoder}
import io.integralla.xapi.model.common.{Decodable, Encodable, Equivalence}
import io.lemonlabs.uri.{UrlPath, UrlWithoutAuthority}

import java.security.MessageDigest
import scala.util.{Failure, Success, Try}

/** An MBOX identifier
  *
  * @param value
  *   A mailto IRI
  */
case class MBox(value: String) extends Encodable[MBox] with Equivalence with StatementValidation {

  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      checkMbox
    )

  }

  /** Returns a SHA1 checksum of the mailto IRI
    *
    * @return
    *   SHA1 checksum
    */
  def shaChecksum: String = {
    MessageDigest
      .getInstance("SHA-1")
      .digest(value.toLowerCase().getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }

  private def checkMbox: Either[String, Boolean] = {
    val parsed: Try[UrlWithoutAuthority] = UrlWithoutAuthority.parseTry(value)
    parsed match {
      case Failure(_) => Left("An Agent mbox identifier must be a valid mailto IRI")
      case Success(value) =>
        if (value.scheme.toLowerCase() != "mailto") {
          Left("An Agent mbox identifier must use the mailto schema")
        } else {
          Right(true)
        }
    }
  }

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * For the purposes of generating the signature, we generate a version of the
    * MBox IRI with the schema and path parts all lower cased
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      UrlWithoutAuthority
        .parseOption(value).map(url => {
          UrlWithoutAuthority(
            scheme = lower(url.scheme),
            path = UrlPath(url.path.parts.map(lower)),
            query = url.query,
            fragment = url.fragment
          ).toString()
        }).getOrElse(value)
    }
  }
}

object MBox extends Decodable[MBox] {
  implicit val encoder: Encoder[MBox] = Encoder.encodeString.contramap[MBox](_.value)
  implicit val decoder: Decoder[MBox] = Decoder.decodeString.map[MBox](MBox.apply)
}
