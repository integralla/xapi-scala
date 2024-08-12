package io.integralla.xapi.model.person

import io.integralla.xapi.model.statement.{Account, MBox}
import org.scalatest.funspec.AnyFunSpec

class PersonTest extends AnyFunSpec {
  describe("Person") {
    describe("[encoding/decoding]") {
      it("should encode/decode a person object") {
        val person: Person = Person(
          objectType = Person.objectType,
          name = Some(List("Lorum Ipsum")),
          mbox = Some(List(MBox("mailto:lorum.ipsum@integralla.io"))),
          mbox_sha1sum = Some(List("9ae85debf7e0cc662c7e0c72cfb1fd0887fdd62b")),
          openid = Some(List("https://identity.integralla.io/Bd6NwLmK")),
          account = Some(List(Account("https://identity.integralla.io/", "Bd6NwLmK")))
        )

        val expected: String =
          """{
            |  "objectType" : "Person",
            |  "name" : [
            |    "Lorum Ipsum"
            |  ],
            |  "mbox" : [
            |    "mailto:lorum.ipsum@integralla.io"
            |  ],
            |  "mbox_sha1sum" : [
            |    "9ae85debf7e0cc662c7e0c72cfb1fd0887fdd62b"
            |  ],
            |  "openid" : [
            |    "https://identity.integralla.io/Bd6NwLmK"
            |  ],
            |  "account" : [
            |    {
            |      "homePage" : "https://identity.integralla.io/",
            |      "name" : "Bd6NwLmK"
            |    }
            |  ]
            |}""".stripMargin

        val encoded: String = person.toJson(spaces = true)
        assert(encoded === expected)

        val decoded: Person = Person(encoded).get
        assert(decoded === person)
      }
      it("should encode/decode a person object, removing null fields") {
        val person: Person = Person(
          objectType = Person.objectType,
          name = Some(List("Lorum Ipsum")),
          mbox = Some(List(MBox("mailto:lorum.ipsum@integralla.io")))
        )

        val expected: String =
          """{
            |  "objectType" : "Person",
            |  "name" : [
            |    "Lorum Ipsum"
            |  ],
            |  "mbox" : [
            |    "mailto:lorum.ipsum@integralla.io"
            |  ]
            |}""".stripMargin

        val encoded: String = person.toJson(spaces = true)
        assert(encoded === expected)

        val decoded: Person = Person(encoded).get
        assert(decoded === person)
      }
      it("should encode/decode a person object with multiple field values") {
        val person: Person = Person(
          objectType = Person.objectType,
          name = Some(List("Lorum Ipsum", "Test User")),
          mbox = Some(List(MBox("mailto:lorum.ipsum@integralla.io")))
        )

        val expected: String =
          """{
            |  "objectType" : "Person",
            |  "name" : [
            |    "Lorum Ipsum",
            |    "Test User"
            |  ],
            |  "mbox" : [
            |    "mailto:lorum.ipsum@integralla.io"
            |  ]
            |}""".stripMargin

        val encoded: String = person.toJson(spaces = true)
        assert(encoded === expected)

        val decoded: Person = Person(encoded).get
        assert(decoded === person)
      }
    }
  }
}
