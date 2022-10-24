package io.integralla.model.xapi.statement.identifiers


import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import io.integralla.model.xapi.statement.StatementValidation
import io.lemonlabs.uri.Uri

import java.security.MessageDigest
import scala.util.{Failure, Success, Try}

/**
 * An International Resource Identifier (IRI)
 *
 * @param value An IRI string
 */
case class IRI(value: String) extends StatementValidation {
  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateIRI
    )
  }

  private def validateIRI: Either[String, Boolean] = {
    val parsed: Try[Uri] = Uri.parseTry(value)
    parsed match {
      case Failure(_) => Left("An IRI must be a valid URI, with a schema")
      case Success(value) =>
        if (value.schemeOption.isEmpty) {
          Left("An IRI must be a valid URI, with a schema")
        }
        else {
          Right(true)
        }
    }
  }

  def toSHA1: String = {
    MessageDigest
      .getInstance("SHA-1")
      .digest(value.toLowerCase().getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }
}

object IRI {
  implicit val encoder: Encoder[IRI] = Encoder.encodeString.contramap[IRI](_.value)
  implicit val decoder: Decoder[IRI] = Decoder.decodeString.map[IRI](IRI.apply)

  implicit val iriKeyEncoder: KeyEncoder[IRI] = (key: IRI) => key.value
  implicit val iriKeyDecoder: KeyDecoder[IRI] = (key: String) => Some(IRI(key))
}