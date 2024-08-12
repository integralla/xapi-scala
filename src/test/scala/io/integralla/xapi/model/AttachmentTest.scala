package io.integralla.xapi.model

import io.integralla.xapi.model.exceptions.StatementValidationException
import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

class AttachmentTest extends AnyFunSpec {

  describe("Attachment") {

    describe("[encoding/decoding]") {
      it("should successfully encode/decode an attachment") {
        val attachment: Attachment = Attachment(
          usageType = IRI("http://adlnet.gov/expapi/attachments/signature"),
          display = LanguageMap(Map("en-US" -> "Signature")),
          description = Some(LanguageMap(Map("en-US" -> "A test signature"))),
          contentType = "application/octet-stream",
          length = 4235,
          sha2 = "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
        )

        val expected: String =
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

        val encoded: String = attachment.toJson(spaces = true)
        assert(encoded === expected)

        val decoded: Try[Attachment] = Attachment(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === attachment)
      }
      it("should successfully encode/decode an attachment, with a fileUrl") {
        val attachment: Attachment = Attachment(
          usageType = IRI("http://example.com/attachment-usage/test"),
          display = LanguageMap(Map("en-US" -> "Test Attachment")),
          description = Some(LanguageMap(Map("en-US" -> "A test attachment"))),
          contentType = "text/plain; charset=ascii",
          length = 27,
          sha2 = "495395e777cd98da653df9615d09c0fd6bb2f8d4788394cd53c56a3bfdcd848a",
          fileUrl = Some(IRI("http://example.com/attachment-storage/test"))
        )

        val expected: String =
          """{
            |  "usageType" : "http://example.com/attachment-usage/test",
            |  "display" : {
            |    "en-US" : "Test Attachment"
            |  },
            |  "description" : {
            |    "en-US" : "A test attachment"
            |  },
            |  "contentType" : "text/plain; charset=ascii",
            |  "length" : 27,
            |  "sha2" : "495395e777cd98da653df9615d09c0fd6bb2f8d4788394cd53c56a3bfdcd848a",
            |  "fileUrl" : "http://example.com/attachment-storage/test"
            |}""".stripMargin

        val encoded: String = attachment.toJson(spaces = true)
        assert(encoded === expected)

        val decoded: Try[Attachment] = Attachment(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === attachment)
      }
    }

    describe("[validation]") {
      it(
        "should throw a validation exception for an signature type attachment with an invalid content type"
      ) {
        val exception = intercept[StatementValidationException] {
          Attachment(
            usageType = IRI("http://adlnet.gov/expapi/attachments/signature"),
            display = LanguageMap(Map("en-US" -> "Signature")),
            description = Some(LanguageMap(Map("en-US" -> "A test signature"))),
            contentType = "application/json",
            length = 4235,
            sha2 = "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
          )
        }
        assert(
          exception.getMessage.contains(
            "The JWS for a signed statement must have the attachment content-type of application/octet-stream"
          )
        )
      }
    }

    describe("isSignature") {
      it("should return true if the attachment is a JWS for a signed statement") {
        val attachment: Attachment = Attachment(
          usageType = IRI("http://adlnet.gov/expapi/attachments/signature"),
          display = LanguageMap(Map("en-US" -> "Signature")),
          description = Some(LanguageMap(Map("en-US" -> "A test signature"))),
          contentType = "application/octet-stream",
          length = 4235,
          sha2 = "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
        )
        assert(attachment.isSignature)
      }
      it("should return false if the attachment is a not JWS for a signed statement") {
        val attachment: Attachment = Attachment(
          usageType = IRI("http://example.com/attachment-usage/test"),
          display = LanguageMap(Map("en-US" -> "Test Attachment")),
          description = Some(LanguageMap(Map("en-US" -> "A test attachment"))),
          contentType = "text/plain; charset=ascii",
          length = 27,
          sha2 = "495395e777cd98da653df9615d09c0fd6bb2f8d4788394cd53c56a3bfdcd848a",
          fileUrl = Some(IRI("http://example.com/attachment-storage/test"))
        )
        assert(!attachment.isSignature)
      }
    }
  }
}
