package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.common.ExtensionMap
import io.integralla.model.xapi.statement.identifiers.IRI
import io.integralla.testing.spec.UnitSpec

class ActivityTest extends UnitSpec {

  val nameLanguageMap: LanguageMap = LanguageMap(Map("en-US" -> "Example Activity", "it-IT" -> "Esempio di attività"))
  val descriptionLanguageMap: LanguageMap = LanguageMap(
    Map("en-US" -> "An xAPI activity", "it-IT" -> "Un'attività xAPI")
  )

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

    describe("[equivalence]") {

      val activityDefinition: ActivityDefinition = ActivityDefinition(
        name = Some(LanguageMap(Map("en" -> "Sample"))),
        description = Some(LanguageMap(Map("en" -> "Sample Activity"))),
        `type` = Some(IRI("https://lrs.integralla.io/xapi/activity-types/homework")),
        moreInfo = Some(IRI("http://adlnet.gov/expapi/activities/cmi.interaction")),
        interactionType = Some(InteractionType.CHOICE),
        correctResponsesPattern = Some(CorrectResponsePattern(List("quartz"))),
        choices = Some(
          List(
            InteractionComponent(id = "quartz", definition = Some(LanguageMap(Map("en" -> "Quartz")))),
            InteractionComponent(id = "silica", definition = Some(LanguageMap(Map("en" -> "Silica")))),
            InteractionComponent(id = "chert", definition = Some(LanguageMap(Map("en" -> "Chert"))))
          )
        ),
        scale = None,
        source = None,
        steps = None,
        target = None,
        extensions = Some(ExtensionMap(Map(IRI("http://lrs.integralla.io/xapi/extenions/string") -> "string".asJson)))
      )

      val activity: Activity = Activity(
        Some(StatementObjectType.Activity),
        IRI("https://example.com/xapi/activity/simplestatement"),
        Some(activityDefinition)
      )

      describe("isEquivalentTo") {
        it("should return true if the identifiers of both activities match") {
          val left = activity.copy()
          val right = activity.copy()
          assert(left.isEquivalentTo(right))
        }
        it("should return true if the identifiers of both activities match (other properties do not)") {
          val left = activity.copy()
          val right = activity.copy(objectType = None, definition = None)
          assert(left.isEquivalentTo(right))
        }
        it("should return false if the identifiers of the activities don't match") {
          val left = activity.copy()
          val right = activity.copy(id = IRI("https://example.com/xapi/activity/other"))
          assert(left.isEquivalentTo(right) === false)
        }
      }

      describe("isEquivalentToFull") {
        it("should return true if the identifiers and definitions match") {
          val left = activity.copy()
          val right = activity.copy()
          assert(left.isEquivalentToFull(right))
        }
        it("should return true if the definitions are both undefined") {
          val left = activity.copy(definition = None)
          val right = activity.copy(definition = None)
          assert(left.isEquivalentToFull(right))
        }
        it("should return false if definitions don't match (change)") {
          val left = activity.copy()
          val right = activity.copy(
            definition = Some(activityDefinition.copy(moreInfo = None))
          )
          assert(left.isEquivalentToFull(right) === false)
        }
        it("should return false if definitions don't match (current is undefined)") {
          val left = activity.copy(definition = None)
          val right = activity.copy()
          assert(left.isEquivalentToFull(right) === false)
        }
        it("should return false if definitions don't match (compared is undefined)") {
          val left = activity.copy()
          val right = activity.copy(definition = None)
          assert(left.isEquivalentToFull(right) === false)
        }
      }
    }
  }

}
