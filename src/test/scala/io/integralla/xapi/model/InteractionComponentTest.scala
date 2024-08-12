package io.integralla.xapi.model

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import org.scalatest.funspec.AnyFunSpec

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
        val interactionComponent: InteractionComponent = sample.copy()
        val encoded: String = interactionComponent.asJson.noSpacesSortKeys
        assert(encoded === """{"definition":{"en-US":"Quartz Crystal","it-IT":"Cristallo di quarzo"},"id":"quartz"}""")
      }

      it("should encode an interaction component without a definition") {
        val interactionComponent: InteractionComponent = sample.copy(
          definition = None
        )
        val encoded: String = interactionComponent.asJson.noSpacesSortKeys
        assert(encoded === """{"id":"quartz"}""")
      }
    }
    describe("[decoding]") {
      it("should decode an interaction component") {
        val raw: String = """{"definition":{"en-US":"Quartz Crystal","it-IT":"Cristallo di quarzo"},"id":"quartz"}"""
        val decoded: Either[io.circe.Error, InteractionComponent] = decode[InteractionComponent](raw)
        decoded match {
          case Right(actual) =>
            assert(actual.id === "quartz")
            assert(actual.definition.get.value("en-US") === "Quartz Crystal")
            assert(actual.definition.get.value("it-IT") === "Cristallo di quarzo")
          case Left(err) => throw err
        }
      }

      it("should decode an interaction component without a definition") {
        val raw: String = """{"id":"quartz"}"""
        val decoded: Either[io.circe.Error, InteractionComponent] = decode[InteractionComponent](raw)
        decoded match {
          case Right(actual) =>
            assert(actual.id === "quartz")
            assert(actual.definition.isEmpty)
          case Left(err) => throw err
        }
      }
    }
    describe("[equivalent]") {
      it("should return true if both interaction components are equivalent") {
        val left: InteractionComponent = sample.copy()
        val right: InteractionComponent = sample.copy()
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both interaction components are equivalent (neither has definition)") {
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
