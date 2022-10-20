package io.integralla.model.xapi.statement.identifiers

import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.{StatementModelBase, StatementModelValidation}
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
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

object Account extends StatementModelBase {

  override type T = Account

  override implicit val decoder: Decoder[Account] = deriveDecoder[Account]
  override implicit val encoder: Encoder[Account] = deriveEncoder[Account]
}