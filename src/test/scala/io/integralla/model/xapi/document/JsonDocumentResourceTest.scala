package io.integralla.model.xapi.document

import io.circe.HCursor
import io.integralla.model.exceptions.JsonObjectValidationException
import io.integralla.testing.spec.UnitSpec

import scala.util.Try

class JsonDocumentResourceTest extends UnitSpec {
  describe("JsonDocumentResource") {
    describe("apply") {
      it("should create a new instance from a valid json object") {
        val document: Try[JsonDocumentResource] = JsonDocumentResource(
          """{
            |	"x": "tata",
            |	"y": 0
            |}""".stripMargin
        )
        assert(document.isSuccess)
        val cursor: HCursor = document.get.json.hcursor
        assert(cursor.get[String]("x").getOrElse("unknown") === "tata")
        assert(cursor.get[Int]("y").getOrElse(-1) === 0)
      }
      it("should return an exception if the value is not a json object") {
        val document: Try[JsonDocumentResource] = JsonDocumentResource("true")
        assert(document.isFailure)
        val exception = intercept[JsonObjectValidationException] {
          document.get
        }
        assert(exception.getMessage.contains("JSON must be a JSON object"))
      }
      it("should return an exception if the json value cannot be parsed") {
        val document: Try[JsonDocumentResource] = JsonDocumentResource("""{"x":}""")
        assert(document.isFailure)
        val exception = intercept[JsonObjectValidationException] {
          document.get
        }
        assert(exception.getMessage.contains("Unable to parse document resource"))
      }
    }

    describe("merge") {
      it("should merge one document with another") {
        val left: JsonDocumentResource = JsonDocumentResource(
          """{
            |	"x": "toto",
            |	"y": "tata"
            |}""".stripMargin
        ).get

        val right: JsonDocumentResource = JsonDocumentResource(
          """{
            |	"y": "titi",
            |	"z": "tutu"
            |}""".stripMargin
        ).get

        val merged: JsonDocumentResource = left.merge(right)

        val cursor: HCursor = merged.json.hcursor
        assert(cursor.get[String]("x").getOrElse("unknown") === "toto")
        assert(cursor.get[String]("y").getOrElse("unknown") === "titi")
        assert(cursor.get[String]("z").getOrElse("unknown") === "tutu")
      }
      it("should only merge top-level properties") {
        val left: JsonDocumentResource = JsonDocumentResource(
          """{
            |	"animals": {
            |		"a": "armadillo"
            |   }
            |}""".stripMargin
        ).get

        val right: JsonDocumentResource = JsonDocumentResource(
          """{
            |	"animals": {
            |		"b": "badger"
            |	}
            |}""".stripMargin
        ).get

        val merged: JsonDocumentResource = left.merge(right)

        val cursor: HCursor = merged.json.hcursor
        assert(cursor.downField("animals").get[String]("a").getOrElse("unknown") === "unknown")
        assert(cursor.downField("animals").get[String]("b").getOrElse("unknown") === "badger")
      }
    }

    describe("toJson") {
      it("should return the document json encoded as a string") {
        val document: JsonDocumentResource = JsonDocumentResource(
          """{
            |	"x": "toto",
            |	"y": "tata"
            |}""".stripMargin
        ).get

        assert(document.toJson === """{"x":"toto","y":"tata"}""")
      }
    }
  }
}
