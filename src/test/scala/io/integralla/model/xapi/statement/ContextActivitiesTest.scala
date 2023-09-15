package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.references.{ActivityReference, CategoryRef, GroupingRef, OtherRef, ParentRef}
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
      it("should successfully encode a context references object") {
        val actual = sampleContextActivities.asJson.noSpaces
        assert(actual === sampleContextActivitiesEncoded)
      }

      it("should successfully encode a context references object in which not all properties are defined") {
        val contextActivities: ContextActivities = ContextActivities(Some(List(sampleParent)), None, None, None)
        val actual = contextActivities.asJson.noSpaces
        val expected: String =
          """{"parent":[{"objectType":"Activity","id":"http://example.adlnet.gov/xapi/example/test"}]}"""
        assert(actual === expected)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a context references object") {
        val decoded: Either[io.circe.Error, ContextActivities] =
          decode[ContextActivities](sampleContextActivitiesEncoded)
        decoded match {
          case Right(actual) => assert(actual === sampleContextActivities)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a context references object in which not all properties are defined") {
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
        "should successfully decode a context references object in which properties are set to a single activity object"
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

    describe("activityReferences") {
      val baseContextActivities: ContextActivities = ContextActivities(
        parent = Some(List(Activity(None, IRI("https://lrs.integralla.io/activity/parent"), None))),
        grouping = Some(List(Activity(None, IRI("https://lrs.integralla.io/activity/grouping"), None))),
        category = Some(List(Activity(None, IRI("https://lrs.integralla.io/activity/category"), None))),
        other = Some(List(Activity(None, IRI("https://lrs.integralla.io/activity/other"), None)))
      )

      it("should return a list of all activity references within the context activities object") {
        val contextActivities: ContextActivities = baseContextActivities.copy()
        val references: List[ActivityReference] = contextActivities.activityReferences()
        val iris: List[String] = references.map(_.activity.id.value)

        assert(references.length === 4)
        assert(iris.contains("https://lrs.integralla.io/activity/parent"))
        assert(iris.contains("https://lrs.integralla.io/activity/grouping"))
        assert(iris.contains("https://lrs.integralla.io/activity/category"))
        assert(iris.contains("https://lrs.integralla.io/activity/other"))

        assert(references.find(_.activity.id.value.endsWith("parent")).get.referenceType === ParentRef)
        assert(references.find(_.activity.id.value.endsWith("grouping")).get.referenceType === GroupingRef)
        assert(references.find(_.activity.id.value.endsWith("category")).get.referenceType === CategoryRef)
        assert(references.find(_.activity.id.value.endsWith("other")).get.referenceType === OtherRef)

        assert(references.map(_.inSubStatement).forall(_ === false))
      }

      it("should set the inSubStatement property to true, if the inSubStatement parameter is true") {
        val contextActivities: ContextActivities = baseContextActivities.copy()
        val references: List[ActivityReference] = contextActivities.activityReferences(true)
        assert(references.map(_.inSubStatement).forall(_ === true))
      }

      it("should return an empty list if there are no activity references") {
        val contextActivities: ContextActivities = baseContextActivities.copy(
          parent = None,
          grouping = None,
          category = None,
          other = None
        )
        val references: List[ActivityReference] = contextActivities.activityReferences()
        assert(references.isEmpty)
      }

    }
  }
}
