package io.integralla.model.xapi.statement.identifiers

import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.StatementValidation
import io.lemonlabs.uri.AbsoluteUrl

import scala.util.{Failure, Success, Try}

/** An account object
  *
  * @param homePage The canonical home page for the system the account is on
  * @param name     The unique id or name used to log in to this account
  */
case class Account(
  homePage: String,
  name: String
) extends StatementValidation with LazyLogging {

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

}

object Account {
  implicit val decoder: Decoder[Account] = deriveDecoder[Account]
  implicit val encoder: Encoder[Account] = deriveEncoder[Account]
}
