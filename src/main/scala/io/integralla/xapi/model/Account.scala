package io.integralla.xapi.model

import com.typesafe.scalalogging.LazyLogging
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.xapi.model.common.Equivalence
import io.lemonlabs.uri.AbsoluteUrl

import scala.util.{Failure, Success, Try}

/** An account object
  *
  * @param homePage
  *   The canonical home page for the system the account is on
  * @param name
  *   The unique id or name used to log in to this account
  */
case class Account(
  homePage: String,
  name: String
) extends StatementValidation with Equivalence with LazyLogging {

  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      checkHomePage
    )
  }

  private def checkHomePage: Either[String, Boolean] = {
    val parsed: Try[AbsoluteUrl] = AbsoluteUrl.parseTry(homePage)
    parsed match {
      case Failure(_) => Left("An Agent account homepage must be a valid URI, with a schema")
      case Success(_) => Right(true)
    }
  }

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * To generate the signature, the account name is appended to the homepage as
    * a URL fragment and the combined value handled as an IRI
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    IRI(s"$homePage#$name").signature()
  }
}

object Account {
  implicit val decoder: Decoder[Account] = deriveDecoder[Account]
  implicit val encoder: Encoder[Account] = deriveEncoder[Account]
}
