package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.identifiers.IRI

/**
 * An attachment represents a resource that is logically associated with the statement
 *
 * @param usageType   Identifies the intended usage of this attachment
 * @param display     The display name (title) of the attachment
 * @param description A description of the attachment
 * @param contentType The Internet Media Type of the attachment
 * @param length      The length of the attachment data in octets
 * @param sha2        A SHA-2 hash of the attachment data
 * @param fileUrl     An IRL at which the attachment data can be retrieved, or from which it used to be retrievable
 */
case class Attachment(
  usageType: IRI,
  display: LanguageMap,
  description: Option[LanguageMap],
  contentType: String,
  length: Int,
  sha2: String,
  fileUrl: Option[IRI]
)

object Attachment {
  implicit val decoder: Decoder[Attachment] = deriveDecoder[Attachment]
  implicit val encoder: Encoder[Attachment] = deriveEncoder[Attachment].mapJson(_.dropNullValues)
}
