package io.integralla.model.xapi.statement.identifiers


import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.{StatementModelBase, StatementModelValidation}
import io.lemonlabs.uri.Uri

import java.security.MessageDigest

/**
 * An International Resource Identifier (IRI)
 *
 * @param value An IRI string
 */
case class IRI(value: String) extends StatementModelValidation {
  override def validate(): Unit = {
    validateIRI()
  }

  private def validateIRI(): Unit = {
    try {
      val parsed = Uri.parse(value)
      if (parsed.schemeOption.isEmpty) {
        throw new StatementValidationException("An IRI must be a valid URI, with a schema")
      }
    } catch {
      case _: Throwable => throw new StatementValidationException("An IRI must be a valid URI, with a schema")
    }
  }

  def toSHA1: String = {
    MessageDigest
      .getInstance("SHA-1")
      .digest(value.toLowerCase().getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }
}

object IRI extends StatementModelBase {
  override type T = IRI
  override implicit val encoder: Encoder[IRI] = Encoder.encodeString.contramap[IRI](_.value)
  override implicit val decoder: Decoder[IRI] = Decoder.decodeString.map[IRI](IRI.apply)

  implicit val iriKeyEncoder: KeyEncoder[IRI] = (key: IRI) => key.value

  implicit val iriKeyDecoder: KeyDecoder[IRI] = (key: String) => Some(IRI(key))
}