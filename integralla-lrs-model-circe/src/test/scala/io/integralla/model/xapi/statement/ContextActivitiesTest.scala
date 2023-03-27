package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.identifiers.IRI
import io.integralla.testing.spec.UnitSpec

class ContextActivitiesTest extends UnitSpec {

  val sampleParent: Activity =
    Activity(Some(StatementObjectType.Activity), IRI("http://example.adlnet.gov/xapi/example/test"), None)
  val sampleGrouping: Activity =
    Activity(Some(StatementObjectType.Activity), IRI("http://example.adlnet.gov/xapi/example/class"), None)
  val sampleCategory: Activity =
    Activity(Some(StatementObjectType.Activity), IRI("http://example.adlnet.gov/xapi/example/category"), None)
  val sampleOther: Activity =
    Activity(Some(StatementObjectType.Activity), IRI("http://example.adlnet.gov/xapi/example/other"), None)

  val sampleContextActivities: ContextActivities = ContextActivities(
    Some(List(sampleParent)),
    Some(List(sampleGrouping)),
    Some(List(sampleCategory)),
    Some(List(sampleOther))
  )
  val sampleContextActivitiesEncoded: String =
    """{"parent":[{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/test"}],"grouping":[{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/class"}],"category":[{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/category"}],"other":[{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/other"}]}"""

  describe("Context Activities") {
    describe("[encoding]") {
      it("should successfully encode a context activities object") {
        val actual = sampleContextActivities.asJson.noSpaces
        assert(actual === sampleContextActivitiesEncoded)
      }

      it("should successfully encode a context activities object in which not all properties are defined") {
        val contextActivities: ContextActivities = ContextActivities(Some(List(sampleParent)), None, None, None)
        val actual = contextActivities.asJson.noSpaces
        val expected: String =
          """{"parent":[{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/test"}]}"""
        assert(actual === expected)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a context activities object") {
        val decoded: Either[io.circe.Error, ContextActivities] =
          decode[ContextActivities](sampleContextActivitiesEncoded)
        decoded match {
          case Right(actual) => assert(actual === sampleContextActivities)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a context activities object in which not all properties are defined") {
        val data: String =
          """{"parent":[{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/test"}]}"""
        val decoded: Either[io.circe.Error, ContextActivities] = decode[ContextActivities](data)
        val expected: ContextActivities = ContextActivities(Some(List(sampleParent)), None, None, None)
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it(
        "should successfully decode a context activities object in which properties are set to a single activity object"
      ) {
        val data: String =
          """{"parent": {"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/test"},"grouping":{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/class"},"category":{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/category"},"other":{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/other"}}"""
        val decoded: Either[io.circe.Error, ContextActivities] = decode[ContextActivities](data)
        decoded match {
          case Right(actual) => assert(actual === sampleContextActivities)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }
    }

    describe("[equivalence]") {
      it("should return true if both context activity objects are equivalent") {
        val left: ContextActivities = sampleContextActivities.copy()
        val right: ContextActivities = sampleContextActivities.copy()
        assert(left.isEquivalentTo(right))
      }

      it("should return true if both context activity objects are equivalent, excepting list order") {
        val left: ContextActivities = ContextActivities(
          parent = None,
          grouping = None,
          category = None,
          other = Some(
            List(
              Activity(Some(StatementObjectType.Activity), IRI("http://example.adlnet.gov/xapi/example/one"), None),
              Activity(Some(StatementObjectType.Activity), IRI("http://example.adlnet.gov/xapi/example/two"), None)
            )
          )
        )

        val right: ContextActivities = ContextActivities(
          parent = None,
          grouping = None,
          category = None,
          other = Some(
            List(
              Activity(Some(StatementObjectType.Activity), IRI("http://example.adlnet.gov/xapi/example/two"), None),
              Activity(Some(StatementObjectType.Activity), IRI("http://example.adlnet.gov/xapi/example/one"), None)
            )
          )
        )

        assert(left.isEquivalentTo(right))
      }

      it("should return false if both context activity objects are not equivalent") {
        val left: ContextActivities = sampleContextActivities.copy()
        val right: ContextActivities = sampleContextActivities.copy(parent = None)
        assert(left.isEquivalentTo(right) === false)
      }
    }

    describe("getActivityReferences") {
      val baseContextActivities: ContextActivities = ContextActivities(
        parent = Some(List(Activity(None, IRI("https://lrs.integralla.io/activity/parent"), None))),
        grouping = Some(List(Activity(None, IRI("https://lrs.integralla.io/activity/grouping"), None))),
        category = Some(List(Activity(None, IRI("https://lrs.integralla.io/activity/category"), None))),
        other = Some(List(Activity(None, IRI("https://lrs.integralla.io/activity/other"), None)))
      )

      it("should return a distinct list of all activities referenced as context activities") {
        val contextActivities: ContextActivities = baseContextActivities.copy()
        val activities: List[Activity] = contextActivities.getActivityReferences
        val iris: List[String] = activities.map(activity => activity.id.value)

        assert(activities.length === 4)
        assert(iris.contains("https://lrs.integralla.io/activity/parent"))
        assert(iris.contains("https://lrs.integralla.io/activity/grouping"))
        assert(iris.contains("https://lrs.integralla.io/activity/category"))
        assert(iris.contains("https://lrs.integralla.io/activity/other"))
      }

      it("should return a distinct list") {
        val contextActivities: ContextActivities = baseContextActivities.copy(
          other = Some(List(Activity(None, IRI("https://lrs.integralla.io/activity/parent"), None)))
        )
        val activities: List[Activity] = contextActivities.getActivityReferences
        assert(activities.length === 3)
      }

      it("should return an empty list if none or defined") {
        val contextActivities: ContextActivities = baseContextActivities.copy(
          parent = None,
          grouping = None,
          category = None,
          other = None
        )
        val activities: List[Activity] = contextActivities.getActivityReferences
        assert(activities.isEmpty)
      }

    }
  }
}
