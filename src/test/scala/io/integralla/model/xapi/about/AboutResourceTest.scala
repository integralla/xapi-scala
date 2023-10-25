package io.integralla.model.xapi.about

import io.circe.syntax.EncoderOps
import io.integralla.model.utils.LRSModel
import io.integralla.model.xapi.common.ExtensionMap
import io.integralla.model.xapi.identifiers.IRI
import io.integralla.testing.spec.UnitSpec

class AboutResourceTest extends UnitSpec {

  describe("AboutResource") {
    describe("[encoding/decoding]") {

      it("should support encoding/decoding json [version only]") {
        val resource = AboutResource(version = List("1.0.3"), extensions = None)
        val encoded: String = resource.toJson[AboutResource]()
        val decoded: AboutResource = LRSModel[AboutResource](encoded).get
        assert(encoded === """{"version":["1.0.3"]}""")
        assert(decoded === resource)
      }

      it("should support encoding/decoding json [with extensions]") {
        val resource = AboutResource(
          version = List("1.0.3", "2.0.0"),
          extensions = Some(ExtensionMap(Map(IRI("https://lrs.integralla.io/docs") -> "API Documentation".asJson)))
        )
        val encoded: String = resource.toJson[AboutResource]()
        val decoded: AboutResource = LRSModel[AboutResource](encoded).get
        assert(
          encoded === """{"version":["1.0.3","2.0.0"],"extensions":{"https://lrs.integralla.io/docs":"API Documentation"}}"""
        )
        assert(decoded === resource)
      }
    }
  }

}
