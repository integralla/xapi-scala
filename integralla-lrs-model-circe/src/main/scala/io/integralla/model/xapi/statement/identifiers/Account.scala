package io.integralla.model.xapi.statement.identifiers

import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.StatementModelValidation
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.lemonlabs.uri.AbsoluteUrl

/**
 * An account object
 *
 * @param homePage The canonical home page for the system the account is on
 * @param name     The unique id or name used to log in to this account
 */
case class Account(
  homePage: String,
  name: String
) extends StatementModelValidation with LazyLogging {

  override def validate(): Unit = {
    checkHomePage()
  }

  private def checkHomePage(): Unit = {
    try {
      val _ = AbsoluteUrl.parse(homePage)
    } catch {
      case _: Throwable => throw new StatementValidationException("An Agent account homepage must be a valid URI, with a schema")
    }
  }

}

object Account {
  implicit val decoder: Decoder[Account] = deriveDecoder[Account]
  implicit val encoder: Encoder[Account] = deriveEncoder[Account]
}