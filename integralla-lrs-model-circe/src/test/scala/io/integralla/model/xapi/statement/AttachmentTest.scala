package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.identifiers.IRI
import io.integralla.testing.spec.UnitSpec

class AttachmentTest extends UnitSpec {

  val sampleAttachment: Attachment = Attachment(
    IRI("http://adlnet.gov/expapi/attachments/signature"),
    LanguageMap(Map("en-US" -> "Signature")),
    Some(LanguageMap(Map("en-US" -> "A test signature"))),
    "application/octet-stream",
    4235,
    "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634",
    None
  )

  val sampleAttachmentEncoded: String =
    """{
      |  "usageType" : "http://adlnet.gov/expapi/attachments/signature",
      |  "display" : {
      |    "en-US" : "Signature"
      |  },
      |  "description" : {
      |    "en-US" : "A test signature"
      |  },
      |  "contentType" : "application/octet-stream",
      |  "length" : 4235,
      |  "sha2" : "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
      |}""".stripMargin

  describe("Attachment") {
    describe("[encoding]") {
      it("should successfully encode an attachment") {
        val actual = sampleAttachment.asJson.spaces2
        assert(actual === sampleAttachmentEncoded)
      }
    }

    describe("[decoding]") {
      it("should successfully decode an attachment") {
        val decoded: Either[io.circe.Error, Attachment] = decode[Attachment](sampleAttachmentEncoded)
        decoded match {
          case Right(actual) => assert(actual === sampleAttachment)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }
    }
  }
}
