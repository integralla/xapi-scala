package io.integralla.model.xapi.statement

import io.circe.generic.auto._
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.identifiers.IRI
import io.integralla.testing.spec.UnitSpec

class ExtensionsTest extends UnitSpec {

  case class Wrapper(extensions: Extensions)

  val sampleExtensions: Extensions = Map(
    IRI("http://example.com/extenions/string") -> "string".asJson,
    IRI("http://example.com/extenions/integer") -> 1.asJson,
    IRI("http://example.com/extenions/number") -> 1.0.asJson,
    IRI("http://example.com/extenions/object") -> Map("Sun" -> 1.9891E30).asJson,
    IRI("http://example.com/extenions/array") -> List(3.asJson, "different".asJson, Map("types" -> "of values").asJson).asJson,
    IRI("http://example.com/extenions/boolean") -> true.asJson,
    IRI("http://example.com/extenions/null") -> None.asJson,
  )
  val sampleExtensionsEncoded: String =
    """{"extensions":{
      |"http://example.com/extenions/string":"string",
      |"http://example.com/extenions/array":[3,"different",{"types":"of values"}],
      |"http://example.com/extenions/object":{"Sun":1.9891E30},
      |"http://example.com/extenions/null":null,
      |"http://example.com/extenions/integer":1,
      |"http://example.com/extenions/boolean":true,
      |"http://example.com/extenions/number":1.0}
      |}""".stripMargin

  describe("Extensions") {
    describe("[encoding]") {
      it("should successfully encode extensions") {
        val actual = Wrapper(sampleExtensions).asJson.noSpaces
        println(actual)
        assert(actual === sampleExtensionsEncoded.replaceAll("\n", ""))
      }
    }

    describe("[decoding]") {
      it("should successfully decode extensions") {
        val decoded: Either[io.circe.Error, Wrapper] = decode[Wrapper](sampleExtensionsEncoded)
        val expected: Wrapper = Wrapper(sampleExtensions)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err) => throw new Error(s"Decoding failed: $err")
        }
      }
    }
  }
}
