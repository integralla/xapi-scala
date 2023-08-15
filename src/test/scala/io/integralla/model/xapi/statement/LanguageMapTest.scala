package io.integralla.model.xapi.statement

import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.integralla.testing.spec.UnitSpec

import java.util.Locale
import scala.jdk.CollectionConverters.*

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

    describe("first") {
      it("should return the first entry in the language map") {
        val languageMap: LanguageMap = LanguageMap(
          Map(
            "en" -> "Hello, World!",
            "it" -> "Ciao mondo!"
          )
        )
        val result: Option[LanguageMap] = languageMap.first
        assert(result.isDefined)
        assert(result.get.value.size === 1)
        assert(result.get.value.head._1 === "en")
        assert(result.get.value.head._2 === "Hello, World!")
      }
      it("should return none if the language map is empty") {
        val languageMap: LanguageMap = LanguageMap(Map.empty[String, String])
        val result: Option[LanguageMap] = languageMap.first
        assert(result.isEmpty)
      }
    }

    describe("lookup") {
      it("should return a language map consisting of a single entry matching the provided tag") {
        val languageMap: LanguageMap = LanguageMap(
          Map(
            "en" -> "Hello, World!",
            "it" -> "Ciao mondo!"
          )
        )
        val result: Option[LanguageMap] = languageMap.lookup("en")
        assert(result.isDefined)
        assert(result.get.value.size === 1)
        assert(result.get.value.head._1 === "en")
        assert(result.get.value.head._2 === "Hello, World!")
      }
      it("should return none if no match was found for the tag") {
        val languageMap: LanguageMap = LanguageMap(
          Map(
            "en" -> "Hello, World!",
            "it" -> "Ciao mondo!"
          )
        )
        val result: Option[LanguageMap] = languageMap.lookup("ch")
        assert(result.isEmpty)
      }
      it("should return none if the language map is empty") {
        val languageMap: LanguageMap = LanguageMap(Map.empty[String, String])
        val result: Option[LanguageMap] = languageMap.lookup("en")
        assert(result.isEmpty)
      }
    }

    describe("preferred") {
      it("should return a single, preferred entry from the language map (single range)") {
        val priorityList: List[Locale.LanguageRange] = Locale.LanguageRange.parse("en").asScala.toList
        val languageMap: LanguageMap = LanguageMap(
          Map(
            "hi" -> "हैलो वर्ल्ड!",
            "en" -> "Hello, World!",
            "it" -> "Ciao mondo!",
            "es" -> "¡Hola Mundo!"
          )
        )

        val result: Option[LanguageMap] = languageMap.preferred(priorityList)
        assert(result.isDefined)
        assert(result.get.value.size === 1)
        assert(result.get.value("en") === "Hello, World!")
      }

      it("should return a single, preferred entry from the language map (multiple ranges)") {
        val priorityList: List[Locale.LanguageRange] = Locale.LanguageRange.parse("zh-Hant-TW, en-US").asScala.toList
        val languageMap: LanguageMap = LanguageMap(
          Map(
            "hi" -> "हैलो वर्ल्ड!",
            "en" -> "Hello, World!",
            "it" -> "Ciao mondo!",
            "es" -> "¡Hola Mundo!"
          )
        )

        val result: Option[LanguageMap] = languageMap.preferred(priorityList)
        assert(result.isDefined)
        assert(result.get.value.size === 1)
        assert(result.get.value("en") === "Hello, World!")
      }
      it("should return the first entry from the map if no match is found in the priority list") {
        val priorityList: List[Locale.LanguageRange] = Locale.LanguageRange.parse("zh-Hant-TW").asScala.toList
        val languageMap: LanguageMap = LanguageMap(
          Map(
            "hi" -> "हैलो वर्ल्ड!",
            "en" -> "Hello, World!",
            "it" -> "Ciao mondo!",
            "es" -> "¡Hola Mundo!"
          )
        )

        val result: Option[LanguageMap] = languageMap.preferred(priorityList)
        assert(result.isDefined)
        assert(result.get.value.size === 1)
        assert(result.get.value("hi") === "हैलो वर्ल्ड!")
      }
      it("should return the first entry from the map if priority list is empty") {
        val priorityList: List[Locale.LanguageRange] = List.empty[Locale.LanguageRange]
        val languageMap: LanguageMap = LanguageMap(
          Map(
            "hi" -> "हैलो वर्ल्ड!",
            "en" -> "Hello, World!",
            "it" -> "Ciao mondo!",
            "es" -> "¡Hola Mundo!"
          )
        )

        val result: Option[LanguageMap] = languageMap.preferred(priorityList)
        assert(result.isDefined)
        assert(result.get.value.size === 1)
        assert(result.get.value("hi") === "हैलो वर्ल्ड!")
      }
      it("should return none if the language map is empty") {
        val priorityList: List[Locale.LanguageRange] = Locale.LanguageRange.parse("en").asScala.toList
        val languageMap: LanguageMap = LanguageMap(Map.empty[String, String])

        val result: Option[LanguageMap] = languageMap.preferred(priorityList)
        assert(result.isEmpty)
      }
    }
  }
}
