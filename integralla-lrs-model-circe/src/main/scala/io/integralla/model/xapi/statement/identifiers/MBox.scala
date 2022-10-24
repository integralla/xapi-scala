package io.integralla.model.xapi.statement.identifiers

import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.StatementValidation
import io.lemonlabs.uri.UrlWithoutAuthority

import java.security.MessageDigest
import scala.util.{Failure, Success, Try}

/**
 * An MBOX identifier
 *
 * @param value A mailto IRI
 */
case class MBox(value: String) extends StatementValidation {
  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      checkMbox
    )
  }

  /**
   * Returns a SHA1 checksum of the mailto IRI
   *
   * @return SHA1 checksum
   */
  def shaChecksum: String = {
    MessageDigest.getInstance("SHA-1")
      .digest(value.toLowerCase().getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }


  private def checkMbox: Either[String, Boolean] = {
    val parsed: Try[UrlWithoutAuthority] = UrlWithoutAuthority.parseTry(value)
    parsed match {
      case Failure(_) => Left("An Agent mbox identifier must be a valid mailto IRI")
      case Success(value) =>
        if (value.scheme != "mailto") {
          Left("An Agent mbox identifier must use the mailto schema")
        }
        else {
          Right(true)
        }
    }
  }
}

object MBox {
  implicit val encoder: Encoder[MBox] = Encoder.encodeString.contramap[MBox](_.value)
  implicit val decoder: Decoder[MBox] = Decoder.decodeString.map[MBox](MBox.apply)
}