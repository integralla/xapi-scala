package io.integralla.xapi.model.about

import io.circe.syntax.EncoderOps
import io.integralla.xapi.model.common.ExtensionMap
import io.integralla.xapi.model.statement.IRI
import io.integralla.xapi.model.utils.LRSModel
import org.scalatest.funspec.AnyFunSpec

class AboutResourceTest extends AnyFunSpec {

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
