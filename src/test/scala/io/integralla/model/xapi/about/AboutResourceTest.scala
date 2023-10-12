package io.integralla.model.xapi.about

import io.integralla.testing.spec.UnitSpec
import io.circe.syntax.EncoderOps
import io.circe.jawn.decode
import io.integralla.model.xapi.common.ExtensionMap
import io.integralla.model.xapi.identifiers.IRI

class AboutResourceTest extends UnitSpec {

  describe("AboutResource") {
    describe("[encoding/decoding]") {

      it("should support encoding/decoding json [version only]") {
        val resource = AboutResource(versions = List("1.0.3"), extensions = None)
        val encoded: String = resource.asJson.noSpaces
        val decoded: AboutResource = decode[AboutResource](encoded).toOption.get
        assert(encoded === """{"versions":["1.0.3"]}""")
        assert(decoded === resource)
      }

      it("should support encoding/decoding json [with extensions]") {
        val resource = AboutResource(
          versions = List("1.0.3", "9274.1.1-2023"),
          extensions = Some(ExtensionMap(Map(IRI("https://lrs.integralla.io/docs") -> "API Documentation".asJson)))
        )
        val encoded: String = resource.asJson.noSpaces
        val decoded: AboutResource = decode[AboutResource](encoded).toOption.get
        assert(
          encoded === """{"versions":["1.0.3","9274.1.1-2023"],"extensions":{"https://lrs.integralla.io/docs":"API Documentation"}}"""
        )
        assert(decoded === resource)
      }
    }
  }

}
