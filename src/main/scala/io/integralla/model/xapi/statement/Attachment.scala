package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.model.xapi.identifiers.IRI
import io.integralla.model.xapi.statement.Attachment.signatureUsageType

/** An attachment represents a resource that is logically associated with the
  * statement
  *
  * @param usageType
  *   Identifies the intended usage of this attachment
  * @param display
  *   The display name (title) of the attachment
  * @param description
  *   A description of the attachment
  * @param contentType
  *   The Internet Media Type of the attachment
  * @param length
  *   The length of the attachment data in octets
  * @param sha2
  *   A SHA-2 hash of the attachment data
  * @param fileUrl
  *   An IRL at which the attachment data can be retrieved, or from which it
  *   used to be retrievable
  */
case class Attachment(
  usageType: IRI,
  display: LanguageMap,
  description: Option[LanguageMap] = None,
  contentType: String,
  length: Int,
  sha2: String,
  fileUrl: Option[IRI] = None
) extends StatementValidation:

  /** Indicates whether the attachment is a JWS for a signed statement
    *
    * @return
    *   True if the usage type is http://adlnet.gov/expapi/attachments/signature
    */
  def isSignature: Boolean = {
    if (usageType == signatureUsageType) true else false
  }

  /** Validates that if the attachment usage type is
    * http://adlnet.gov/expapi/attachments/signature, the content type is
    * application/octet-stream
    *
    * @return
    *   Sequence whose values are an `Either` where `Left` provides a
    *   description of validation exception and `Right` represents a boolean
    *   indicating that validation succeeded
    */
  override def validate: Seq[Either[String, Boolean]] =
    Seq(
      if (isSignature) {
        if (contentType.startsWith("application/octet-stream")) { Right(true) }
        else {
          Left("The JWS for a signed statement must have the attachment content-type of application/octet-stream")
        }
      } else { Right(true) }
    )

object Attachment {

  /** The IRI value expected for a signed statement */
  val signatureUsageType: IRI = IRI("http://adlnet.gov/expapi/attachments/signature")

  implicit val decoder: Decoder[Attachment] = deriveDecoder[Attachment]
  implicit val encoder: Encoder[Attachment] = deriveEncoder[Attachment].mapJson(_.dropNullValues)
}
