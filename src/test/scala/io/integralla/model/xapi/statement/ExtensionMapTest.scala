package io.integralla.model.xapi.statement

import com.typesafe.scalalogging.StrictLogging
import io.circe.parser.*
import io.circe.syntax.EncoderOps
import io.circe.ParsingFailure
import io.integralla.model.xapi.common.ExtensionMap
import io.integralla.model.xapi.statement.identifiers.IRI
import io.integralla.testing.spec.UnitSpec

class ExtensionMapTest extends UnitSpec with StrictLogging {

  val extensions: ExtensionMap = ExtensionMap(
    Map(
      IRI("https://lrs.integralla.io/extensions/array") -> List(
        3.asJson,
        "different".asJson,
        Map("types" -> "of values").asJson
      ).asJson,
      IRI("https://lrs.integralla.io/extensions/boolean") -> true.asJson,
      IRI("https://lrs.integralla.io/extensions/integer") -> 100.asJson,
      IRI("https://lrs.integralla.io/extensions/null") -> None.asJson,
      IRI("https://lrs.integralla.io/extensions/number") -> 1.9891e30.asJson,
      IRI("https://lrs.integralla.io/extensions/object") -> parse(
        """
          |{
          | "array": [3,"different",{"types":"of values"}],
          | "boolean": true,
          | "integer": 100,
          | "null": null,
          | "number": 1.9891E30,
          | "object": {
          |   "one": 1
          | },
          | "string": "text"
          |}
          |""".stripMargin
      ).toOption.get,
      IRI("https://lrs.integralla.io/extensions/string") -> "text".asJson
    )
  )

  val json: String =
    """
      |{
      |"https://lrs.integralla.io/extensions/array":[3,"different",{"types":"of values"}],
      |"https://lrs.integralla.io/extensions/boolean":true,
      |"https://lrs.integralla.io/extensions/integer":100,
      |"https://lrs.integralla.io/extensions/null":null,
      |"https://lrs.integralla.io/extensions/number":1.9891E30,
      |"https://lrs.integralla.io/extensions/object":{"array":[3,"different",{"types":"of values"}],"boolean":true,"integer":100,"null":null,"number":1.9891E30,"object":{"one":1},"string":"text"},
      |"https://lrs.integralla.io/extensions/string":"text"}
      |""".stripMargin.replaceAll("\n", "")

  describe("ExtensionMap") {
    describe("[encoding]") {
      it("should encode an extension map") {
        val encoded = extensions.asJson
        logger.info("Encoded:\n" + encoded.noSpacesSortKeys)
        assert(encoded.noSpacesSortKeys === json)
      }
    }

    describe("[decoding]") {
      it("should decode an extension map") {
        val decoded: Either[io.circe.Error, ExtensionMap] = decode[ExtensionMap](json)
        decoded match {
          case Right(actual) => assert(actual === extensions)
          case Left(_)       => false
        }
      }

      it("should throw an exception if the extension map values are not json") {
        val json = """"https://lrs.integralla.io/extensions/uuid": e8aaf354-e6b1-47ba-b522-aa6904aaa1f7}"""
        val decoded: Either[io.circe.Error, ExtensionMap] = decode[ExtensionMap](json)
        assertThrows[ParsingFailure] {
          decoded.toTry.get
        }
      }
    }

    describe("[equivalence]") {
      it("should return true of both extension maps are equivalent") {
        val left: ExtensionMap = ExtensionMap(
          Map(
            IRI("https://lrs.integralla.io/extensions/boolean") -> true.asJson,
            IRI("https://lrs.integralla.io/extensions/object") -> parse("""{"one": 1, "two": 2}""").toOption.get
          )
        )
        val right = left.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return true of both extension maps are equivalent, excepting key order") {
        val left: ExtensionMap = ExtensionMap(
          Map(
            IRI("https://lrs.integralla.io/extensions/object") -> true.asJson,
            IRI("https://lrs.integralla.io/extensions/integer") -> parse("""{"one": 1, "two": 2}""").toOption.get
          )
        )
        val right: ExtensionMap = ExtensionMap(
          Map(
            IRI("https://lrs.integralla.io/extensions/integer") -> parse("""{"one": 1, "two": 2}""").toOption.get,
            IRI("https://lrs.integralla.io/extensions/object") -> true.asJson
          )
        )
        assert(left.isEquivalentTo(right))
      }

      it("should return true of both extension maps are equivalent, excepting IRI exceptions [case]") {
        val left: ExtensionMap = ExtensionMap(
          Map(
            IRI("https://lrs.integralla.io/extensions/object") -> true.asJson,
            IRI("https://lrs.integralla.io/extensions/integer") -> parse("""{"one": 1, "two": 2}""").toOption.get
          )
        )
        val right: ExtensionMap = ExtensionMap(
          Map(
            IRI("https://LRS.INTEGRALLA.IO/extensions/object") -> true.asJson,
            IRI("https://LRS.INTEGRALLA.IO/extensions/integer") -> parse("""{"one": 1, "two": 2}""").toOption.get
          )
        )
        assert(left.isEquivalentTo(right))
      }

      it("should return false if both extension maps are not equivalent") {
        val left: ExtensionMap = ExtensionMap(
          Map(
            IRI("https://lrs.integralla.io/extensions/object") -> parse("""{"one": 1, "two": 2}""").toOption.get,
            IRI("https://lrs.integralla.io/extensions/boolean") -> true.asJson
          )
        )
        val right: ExtensionMap = ExtensionMap(
          Map(
            IRI("https://lrs.integralla.io/extensions/object") -> parse("""{"one": 1, "two": 2}""").toOption.get,
            IRI("https://lrs.integralla.io/extensions/boolean") -> false.asJson
          )
        )
        assert(left.isEquivalentTo(right) === false)
      }
    }
  }
}
