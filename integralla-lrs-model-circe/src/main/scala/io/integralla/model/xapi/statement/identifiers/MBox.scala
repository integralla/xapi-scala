package io.integralla.model.xapi.statement.identifiers

import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.{StatementModelBase, StatementModelValidation}
import io.lemonlabs.uri.UrlWithoutAuthority

import java.security.MessageDigest

/**
 * An MBOX identifier
 *
 * @param value A mailto IRI
 */
case class MBox(value: String) extends StatementModelValidation {
  override def validate(): Unit = {
    checkMbox()
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


  private def checkMbox(): Unit = {
    try {
      val mailto = UrlWithoutAuthority.parse(value)
      if (mailto.scheme != "mailto") throw new StatementValidationException("An Agent mbox identifier must use the mailto schema")
    } catch {
      case _: Throwable => throw new StatementValidationException("An Agent mbox identifier must be a valid mailto IRI")
    }
  }
}

object MBox extends StatementModelBase {
  override type T = MBox
  override implicit val encoder: Encoder[MBox] = Encoder.encodeString.contramap[MBox](_.value)
  override implicit val decoder: Decoder[MBox] = Decoder.decodeString.map[MBox](MBox.apply)
}