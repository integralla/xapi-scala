package io.integralla.model.xapi.statement

import io.integralla.model.utils.LRSModelUtils
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import scala.util.Try

class ContextGroupTest extends UnitSpec {
  describe("ContextGroup") {
    describe("[encoding/decoding]") {
      it("should encode/decode a context group object") {
        val contextGroup: ContextGroup = ContextGroup(
          objectType = "contextGroup",
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Identified Group"),
            mbox = Some(MBox("mailto:identified.group@integralla.io")),
            mbox_sha1sum = None,
            openid = None,
            account = None,
            member = Some(
              List(
                Agent(None, None, Some(MBox("mailto:member.one@example.com")), None, None, None),
                Agent(None, None, Some(MBox("mailto:member.two@example.com")), None, None, None)
              )
            )
          ),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/team"),
              IRI("https://lrs.integralla.io/types/leads")
            )
          )
        )

        val encoded: String = LRSModelUtils.toJSON[ContextGroup](contextGroup, spaces = true)
        val expected: String =
          """{
            |  "objectType" : "contextGroup",
            |  "group" : {
            |    "objectType" : "Group",
            |    "name" : "Identified Group",
            |    "mbox" : "mailto:identified.group@integralla.io",
            |    "member" : [
            |      {
            |        "mbox" : "mailto:member.one@example.com"
            |      },
            |      {
            |        "mbox" : "mailto:member.two@example.com"
            |      }
            |    ]
            |  },
            |  "relevantTypes" : [
            |    "https://lrs.integralla.io/types/team",
            |    "https://lrs.integralla.io/types/leads"
            |  ]
            |}""".stripMargin

        assert(encoded === expected)

        val decoded: Try[ContextGroup] = LRSModelUtils.fromJSON[ContextGroup](encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === contextGroup)
      }
      it("should encode/decode a context group object without any relevant types") {
        val contextGroup: ContextGroup = ContextGroup(
          objectType = "contextGroup",
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Anonymous Group"),
            mbox = None,
            mbox_sha1sum = None,
            openid = None,
            account = None,
            member = Some(
              List(
                Agent(None, None, Some(MBox("mailto:member.one@example.com")), None, None, None),
                Agent(None, None, Some(MBox("mailto:member.two@example.com")), None, None, None)
              )
            )
          ),
          relevantTypes = None
        )

        val encoded: String = LRSModelUtils.toJSON[ContextGroup](contextGroup, spaces = true)
        val expected: String =
          """{
            |  "objectType" : "contextGroup",
            |  "group" : {
            |    "objectType" : "Group",
            |    "name" : "Anonymous Group",
            |    "member" : [
            |      {
            |        "mbox" : "mailto:member.one@example.com"
            |      },
            |      {
            |        "mbox" : "mailto:member.two@example.com"
            |      }
            |    ]
            |  }
            |}""".stripMargin

        assert(encoded === expected)

        val decoded: Try[ContextGroup] = LRSModelUtils.fromJSON[ContextGroup](encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === contextGroup)
      }
    }

    describe("[equivalence]") {
      it("should return true if both objects are equivalent (no relevant types)") {
        val a: ContextGroup = ContextGroup(
          objectType = "contextGroup",
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Anonymous Group"),
            mbox = None,
            mbox_sha1sum = None,
            openid = None,
            account = None,
            member = Some(
              List(
                Agent(None, None, Some(MBox("mailto:member.one@example.com")), None, None, None),
                Agent(None, None, Some(MBox("mailto:member.two@example.com")), None, None, None)
              )
            )
          ),
          relevantTypes = None
        )
        val b: ContextGroup = a.copy()
        assert(a.isEquivalentTo(b))
      }
      it("should return true if both objects are equivalent (with relevant types)") {
        val a: ContextGroup = ContextGroup(
          objectType = "contextGroup",
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Identified Group"),
            mbox = Some(MBox("mailto:identified.group@integralla.io")),
            mbox_sha1sum = None,
            openid = None,
            account = None,
            member = Some(
              List(
                Agent(None, None, Some(MBox("mailto:member.one@example.com")), None, None, None),
                Agent(None, None, Some(MBox("mailto:member.two@example.com")), None, None, None)
              )
            )
          ),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/team"),
              IRI("https://lrs.integralla.io/types/leads")
            )
          )
        )
        val b: ContextGroup = a.copy()
        assert(a.isEquivalentTo(b))
      }
      it("should return true if both objects are equivalent excepting relevant types list order") {
        val a: ContextGroup = ContextGroup(
          objectType = "contextGroup",
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Identified Group"),
            mbox = Some(MBox("mailto:identified.group@integralla.io")),
            mbox_sha1sum = None,
            openid = None,
            account = None,
            member = Some(
              List(
                Agent(None, None, Some(MBox("mailto:member.one@example.com")), None, None, None),
                Agent(None, None, Some(MBox("mailto:member.two@example.com")), None, None, None)
              )
            )
          ),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/team"),
              IRI("https://lrs.integralla.io/types/leads")
            )
          )
        )
        val b: ContextGroup = a.copy(
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/leads"),
              IRI("https://lrs.integralla.io/types/team")
            )
          )
        )
        assert(a.isEquivalentTo(b))
      }
      it("should return false if the objects are not equivalent") {
        val a: ContextGroup = ContextGroup(
          objectType = "contextGroup",
          group = Group(
            objectType = StatementObjectType.Group,
            name = Some("Identified Group"),
            mbox = Some(MBox("mailto:identified.group@integralla.io")),
            mbox_sha1sum = None,
            openid = None,
            account = None,
            member = Some(
              List(
                Agent(None, None, Some(MBox("mailto:member.one@example.com")), None, None, None),
                Agent(None, None, Some(MBox("mailto:member.two@example.com")), None, None, None)
              )
            )
          ),
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/team"),
              IRI("https://lrs.integralla.io/types/leads")
            )
          )
        )
        val b: ContextGroup = a.copy(
          relevantTypes = Some(
            List(
              IRI("https://lrs.integralla.io/types/team")
            )
          )
        )
        assert(a.isEquivalentTo(b) === false)
      }
    }

    describe("[validation]") {
      it("should throw a validation error if the object type value is not recognized") {
        assertThrows[StatementValidationException] {
          ContextGroup(
            objectType = "tata",
            group = Group(
              objectType = StatementObjectType.Group,
              name = Some("Anonymous Group"),
              mbox = None,
              mbox_sha1sum = None,
              openid = None,
              account = None,
              member = Some(
                List(
                  Agent(None, None, Some(MBox("mailto:member.one@example.com")), None, None, None),
                  Agent(None, None, Some(MBox("mailto:member.two@example.com")), None, None, None)
                )
              )
            ),
            relevantTypes = None
          )
        }
      }
    }
  }
}
