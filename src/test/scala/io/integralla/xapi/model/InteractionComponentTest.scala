package io.integralla.xapi.model

import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

class InteractionComponentTest extends AnyFunSpec {
  describe("InteractionComponent") {

    val sample: InteractionComponent = InteractionComponent(
      id = "quartz",
      definition = Some(
        LanguageMap(
          Map(
            "en-US" -> "Quartz Crystal",
            "it-IT" -> "Cristallo di quarzo"
          )
        )
      )
    )

    describe("[encoding]") {
      it("should encode an interaction component") {
        val interactionComponent: InteractionComponent = sample
        val encoded: String = interactionComponent.toJson()
        assert(
          encoded === """{"id":"quartz","definition":{"en-US":"Quartz Crystal","it-IT":"Cristallo di quarzo"}}"""
        )
      }

      it("should encode an interaction component without a definition") {
        val interactionComponent: InteractionComponent = sample.copy(
          definition = None
        )
        val encoded: String = interactionComponent.toJson()
        assert(encoded === """{"id":"quartz"}""")
      }
    }
    describe("[decoding]") {
      it("should decode an interaction component") {
        val raw: String =
          """{"definition":{"en-US":"Quartz Crystal","it-IT":"Cristallo di quarzo"},"id":"quartz"}"""
        val decoded: Try[InteractionComponent] = InteractionComponent(raw)
        assert(decoded.isSuccess)
        assert(decoded.get.id === "quartz")
        assert(decoded.get.definition.get.value("en-US") === "Quartz Crystal")
        assert(decoded.get.definition.get.value("it-IT") === "Cristallo di quarzo")
      }

      it("should decode an interaction component without a definition") {
        val raw: String = """{"id":"quartz"}"""
        val decoded: Try[InteractionComponent] = InteractionComponent(raw)
        assert(decoded.isSuccess)
        assert(decoded.get.id === "quartz")
        assert(decoded.get.definition.isEmpty)
      }
    }
    describe("[equivalent]") {
      it("should return true if both interaction components are equivalent") {
        val left: InteractionComponent = sample.copy()
        val right: InteractionComponent = sample.copy()
        assert(left.isEquivalentTo(right))
      }

      it(
        "should return true if both interaction components are equivalent (neither has definition)"
      ) {
        val left: InteractionComponent = sample.copy(definition = None)
        val right: InteractionComponent = sample.copy(definition = None)
        assert(left.isEquivalentTo(right))
      }

      it("should return false if the interaction components are not equivalent") {
        val left: InteractionComponent = sample.copy(definition = None)
        val right: InteractionComponent = sample.copy(id = "quarzo")
        assert(left.isEquivalentTo(right) === false)
      }
    }
  }
}
