package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.identifiers.IRI
import io.integralla.testing.spec.UnitSpec

class ActivityTest extends UnitSpec {

  val nameLanguageMap: LanguageMap = Map("en-US" -> "Example Activity", "it-IT" -> "Esempio di attività")
  val descriptionLanguageMap: LanguageMap = Map("en-US" -> "An xAPI activity", "it-IT" -> "Un'attività xAPI")

  val sampleActivityDefinition: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  describe("Activity") {
    describe("[encoding]") {
      it("should successfully encode an activity") {
        val activity: Activity = Activity(
          Some(StatementObjectType.Activity),
          IRI("http://example.com/xapi/activity/simplestatement"),
          Some(sampleActivityDefinition)
        )
        val actual = activity.asJson.noSpaces
        val expected =
          """{"objectType":"Activity","id":"http://example.com/xapi/activity/simplestatement","definition":{"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"}}}"""
        assert(actual === expected)
      }

      it("should successfully encode an activity without a definition") {
        val activity: Activity =
          Activity(Some(StatementObjectType.Activity), IRI("http://example.com/xapi/activity/simplestatement"), None)
        val actual = activity.asJson.noSpaces
        val expected = """{"objectType":"Activity","id":"http://example.com/xapi/activity/simplestatement"}"""
        assert(actual === expected)
      }
    }

    describe("[decoding]") {
      it("should successfully decode an activity") {
        val data: String =
          """{"objectType":"Activity","id":"http://example.com/xapi/activity/simplestatement","definition":{"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"}}}"""
        val decoded: Either[io.circe.Error, Activity] = decode[Activity](data)
        val expected: Activity = Activity(
          Some(StatementObjectType.Activity),
          IRI("http://example.com/xapi/activity/simplestatement"),
          Some(sampleActivityDefinition)
        )
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode an activity without a definition") {
        val data: String = """{"objectType":"Activity","id":"http://example.com/xapi/activity/simplestatement"}"""
        val decoded: Either[io.circe.Error, Activity] = decode[Activity](data)
        val expected: Activity =
          Activity(Some(StatementObjectType.Activity), IRI("http://example.com/xapi/activity/simplestatement"), None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode an activity where the objectType has not been explicitly set") {
        val data: String = """{"id":"http://example.com/xapi/activity/simplestatement"}"""
        val decoded: Either[io.circe.Error, Activity] = decode[Activity](data)
        val expected: Activity = Activity(None, IRI("http://example.com/xapi/activity/simplestatement"), None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }
    }
  }

}
