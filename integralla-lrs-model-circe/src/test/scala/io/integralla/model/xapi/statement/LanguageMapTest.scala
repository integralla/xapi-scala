package io.integralla.model.xapi.statement

import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.integralla.testing.spec.UnitSpec

class LanguageMapTest extends UnitSpec {
  describe("LanguageMap") {
    describe("[encoding]") {
      it("should encode a language map") {
        val languageMap: LanguageMap = LanguageMap(
          Map(
            "en" -> "Hello, World!",
            "es" -> "¡Hola Mundo!",
            "it" -> "Ciao mondo!",
            "hi" -> "हैलो वर्ल्ड!"
          )
        )

        val encoded: String = languageMap.asJson.noSpacesSortKeys
        assert(encoded === """{"en":"Hello, World!","es":"¡Hola Mundo!","hi":"हैलो वर्ल्ड!","it":"Ciao mondo!"}""")
      }
    }
  }

  describe("[decoding]") {
    it("should decode a language map") {
      val raw: String = """{"en":"Hello, World!","es":"¡Hola Mundo!","hi":"हैलो वर्ल्ड!","it":"Ciao mondo!"}"""
      val decoded: Either[io.circe.Error, LanguageMap] = decode[LanguageMap](raw)
      decoded match {
        case Right(actual) =>
          assert(actual.value.size === 4)
          assert(actual.value("en") === "Hello, World!")
          assert(actual.value("es") === "¡Hola Mundo!")
          assert(actual.value("it") === "Ciao mondo!")
          assert(actual.value("hi") === "हैलो वर्ल्ड!")
        case Left(err) => throw err
      }
    }
  }

  describe("[equivalence]") {

    val sample: LanguageMap = LanguageMap(
      Map(
        "en" -> "Hello, World!",
        "es" -> "¡Hola Mundo!",
        "it" -> "Ciao mondo!",
        "hi" -> "हैलो वर्ल्ड!"
      )
    )

    it("should return true if both language maps are equivalent") {
      val left: LanguageMap = sample.copy()
      val right: LanguageMap = sample.copy()
      assert(left.isEquivalentTo(right))
    }

    it("should return true if both language maps are equivalent excepting order") {
      val left: LanguageMap = sample.copy()
      val right: LanguageMap = LanguageMap(
        Map(
          "hi" -> "हैलो वर्ल्ड!",
          "en" -> "Hello, World!",
          "it" -> "Ciao mondo!",
          "es" -> "¡Hola Mundo!"
        )
      )
      assert(left.isEquivalentTo(right))
    }

    it("should return true if both language maps are equivalent excepting language code case") {
      val left: LanguageMap = LanguageMap(Map("en-US" -> "Hello, World!"))
      val right: LanguageMap = LanguageMap(Map("en-us" -> "Hello, World!"))
      assert(left.isEquivalentTo(right))
    }

    it("should return false if the language maps are not equivalent") {
      val left: LanguageMap = LanguageMap(Map("en-US" -> "Hello, World!"))
      val right: LanguageMap = LanguageMap(Map("en-us" -> "Greetings, World!"))
      assert(left.isEquivalentTo(right) === false)
    }
  }
}
