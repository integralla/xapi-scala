package io.integralla.xapi.model.utils

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.xapi.model.exceptions.ModelDecodingException
import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

class LRSModelTest extends AnyFunSpec {

  private case class SampleModel(x: Double, y: Double, note: Option[String]) extends LRSModel
  private object SampleModel {
    implicit val decoder: Decoder[SampleModel] = deriveDecoder[SampleModel]
    implicit val encoder: Encoder[SampleModel] = deriveEncoder[SampleModel].mapJson(_.dropNullValues)
  }

  describe("LRSModel") {
    describe("apply") {
      it("should construct a new instance of the model from a json representation") {
        val json: String = """{"x":1.0,"y":0.5,"note":"test"}"""
        val model: SampleModel = LRSModel[SampleModel](json).get
        assert(model.x === 1.0)
        assert(model.y === 0.5)
        assert(model.note.get === "test")
      }
      it("should construct a new instance of the model from a json representation (null value)") {
        val json: String = """{"x":1.0,"y":0.5}"""
        val model: SampleModel = LRSModel[SampleModel](json).get
        assert(model.x === 1.0)
        assert(model.y === 0.5)
        assert(model.note.isEmpty)
      }
      it("should return an exception if the json cannot be decoded") {
        val json: String = """{"x":1.0}"""
        val model: Try[SampleModel] = LRSModel[SampleModel](json)
        assert(model.isFailure)
        val exception = intercept[ModelDecodingException] { model.get }
        assert(exception.getMessage.startsWith("Unable to decode json representation into type"))
      }
      it("should return an exception if the json cannot be parsed") {
        val json: String = """toto"""
        val model: Try[SampleModel] = LRSModel[SampleModel](json)
        assert(model.isFailure)
        val exception = intercept[ModelDecodingException] {
          model.get
        }
        assert(exception.getMessage.startsWith("Unable to decode json representation into type"))
      }
    }

    describe("toJson") {
      it("should return a json representation of the model") {
        val model: SampleModel = SampleModel(1.0, 0.5, Some("test"))
        val encoded: String = model.toJson[SampleModel]()

        val expected: String = """{"x":1.0,"y":0.5,"note":"test"}"""

        assert(encoded === expected)
      }
      it("should return a json representation of the model, dropping nulls") {
        val model: SampleModel = SampleModel(1.0, 0.5, None)
        val encoded: String = model.toJson[SampleModel]()

        val expected: String = """{"x":1.0,"y":0.5}"""

        assert(encoded === expected)
      }
      it("should return a json representation of the model (spaces)") {
        val model: SampleModel = SampleModel(1.0, 0.5, Some("test"))
        val encoded: String = model.toJson[SampleModel](spaces = true)

        val expected: String =
          """{
            |  "x" : 1.0,
            |  "y" : 0.5,
            |  "note" : "test"
            |}""".stripMargin

        assert(encoded === expected)
      }
      it("should return a json representation of the model (sorted)") {
        val model: SampleModel = SampleModel(1.0, 0.5, Some("test"))
        val encoded: String = model.toJson[SampleModel](sorted = true)

        val expected: String = """{"note":"test","x":1.0,"y":0.5}"""

        assert(encoded === expected)
      }
      it("should return a json representation of the model (spaces, sorted)") {
        val model: SampleModel = SampleModel(1.0, 0.5, Some("test"))
        val encoded: String = model.toJson[SampleModel](spaces = true, sorted = true)

        val expected: String =
          """{
            |  "note" : "test",
            |  "x" : 1.0,
            |  "y" : 0.5
            |}""".stripMargin

        assert(encoded === expected)
      }
    }
  }
}
