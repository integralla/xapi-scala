package io.integralla.xapi.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.xapi.model.common.{Decodable, Encodable}

/** xAPI Person model
  *
  * @param objectType
  *   Static value that must be set to "Person"
  * @param name
  *   List of names
  * @param mbox
  *   List of mbox values
  * @param mbox_sha1sum
  *   List of mbox_sha1sum values
  * @param openid
  *   List of OpenId URIs
  * @param account
  *   List of account objects
  */
case class Person(
  objectType: String,
  name: Option[List[String]] = None,
  mbox: Option[List[MBox]] = None,
  mbox_sha1sum: Option[List[String]] = None,
  openid: Option[List[String]] = None,
  account: Option[List[Account]] = None
) extends Encodable[Person]

object Person extends Decodable[Person] {

  /** Required value for the objectType property */
  val objectType: String = "Person"

  /** Implicit encoder/decoder instances */
  implicit val decoder: Decoder[Person] = deriveDecoder[Person]
  implicit val encoder: Encoder[Person] = deriveEncoder[Person].mapJson(_.dropNullValues)
}
