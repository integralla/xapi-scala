package io.integralla.model.xapi.statement.identifiers

import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import io.integralla.model.xapi.statement.{Equivalence, StatementValidation}
import io.lemonlabs.uri.{QueryString, Uri, Url}

import scala.util.{Failure, Success, Try}

/** An International Resource Identifier (IRI)
  *
  * @param value An IRI string
  */
case class IRI(value: String) extends StatementValidation with Equivalence {
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
        } else {
          Right(true)
        }
    }
  }

  override protected[statement] def signature(): String = {
    hash {
      Url
        .parseOption(value).map(url => {
        Url(
          scheme = url.schemeOption.map(lower).orNull,
          user = url.user.orNull,
          password = url.password.orNull,
          host = url.hostOption.map(host => lower(host.value)).orNull,
          port = url.port.getOrElse(-1),
          path = url.path.toString(),
          query = QueryString.apply(url.query.params.sorted),
          fragment = url.fragment.orNull
        ).toString()
      }).getOrElse(value)
    }
  }
}

object IRI {
  implicit val encoder: Encoder[IRI] = Encoder.encodeString.contramap[IRI](_.value)
  implicit val decoder: Decoder[IRI] = Decoder.decodeString.map[IRI](IRI.apply)

  implicit val iriKeyEncoder: KeyEncoder[IRI] = (key: IRI) => key.value
  implicit val iriKeyDecoder: KeyDecoder[IRI] = (key: String) => Some(IRI(key))
}
