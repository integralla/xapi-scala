package io.integralla.model.utils

import com.typesafe.scalalogging.LazyLogging
import io.integralla.model.xapi.exceptions.LRSModelDecodingException
import io.integralla.model.xapi.identifiers.{IRI, MBox}
import io.integralla.model.xapi.statement.*
import io.integralla.testing.spec.UnitSpec

import scala.io.Source
import scala.util.{Try, Using}

class LRSModelUtilsTest extends UnitSpec with LazyLogging {

  describe("LRSModelUtils") {
    describe("toJSON") {
      it("should encode a model object as json (activity)") {
        val activity: Activity = Activity(
          Some(StatementObjectType.Activity),
          IRI("https://lrs.integralla.io/xapi/activity/test"),
          None
        )
        val encoded: String = LRSModelUtils.toJSON(activity)
        assert(encoded === """{"objectType":"Activity","id":"https://lrs.integralla.io/xapi/activity/test"}""")
      }

      it("should encode a model object as json (actor)") {
        val actor: StatementActor = Agent(
          Some(StatementObjectType.Agent),
          Some("John Doe"),
          Some(MBox("mailto:john.doe@example.com")),
          None,
          None,
          None
        )
        val encoded: String = LRSModelUtils.toJSON(actor)
        val expected: String = """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
        assert(encoded === expected)
      }
    }

    describe("fromJSON") {
      it("should decode a JSON string into the specified type (statement)") {
        val encoded: String = Using.resource(Source.fromResource("data/sample-statement-simplest.json"))(_.mkString)
        val decoded: Try[Statement] = LRSModelUtils.fromJSON[Statement](encoded)
        logger.info(s"Decoded:\n$decoded")
        assert(decoded.isSuccess)
        assert(decoded.get.id.get.toString === "12345678-1234-5678-1234-567812345678")
      }
      it("should decode a JSON string into the specified type (agent)") {
        val encoded: String = """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
        val decoded: Try[Agent] = LRSModelUtils.fromJSON[Agent](encoded)
        assert(decoded.isSuccess)
        assert(decoded.get.mbox.get.value === "mailto:john.doe@example.com")
      }
      it("should decode a JSON string into the specified type (actor)") {
        val encoded: String = """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
        val decoded: Try[StatementActor] = LRSModelUtils.fromJSON[StatementActor](encoded)
        assert(decoded.isSuccess)
        assert(decoded.get.mbox.get.value === "mailto:john.doe@example.com")
      }
      it("should return an exception if the JSON string cannot be decoded into the specified type") {
        val encoded: String = """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
        val decoded: Try[Statement] = LRSModelUtils.fromJSON[Statement](encoded)
        assert(decoded.isFailure)
        val caught = intercept[LRSModelDecodingException] {
          decoded.get
        }
        assert(caught.getMessage.contains("DecodingFailure"))
      }
      it("should return an exception if the JSON string cannot be parsed") {
        val encoded: String = """Content-Type: application/json"""
        val decoded: Try[Statement] = LRSModelUtils.fromJSON[Statement](encoded)
        assert(decoded.isFailure)
        val caught = intercept[LRSModelDecodingException] {
          decoded.get
        }
        assert(caught.getMessage.contains("ParsingFailure"))
      }
    }
  }

}
