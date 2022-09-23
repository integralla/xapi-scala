package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{Account, MBox}
import io.integralla.testing.spec.UnitSpec

class StatementActorTest extends UnitSpec {

  describe("An Actor") {

    describe("Agent") {
      describe("[encoding]") {
        it("should successfully encode an agent (mbox)") {
          val actor: StatementActor = Agent(Some(StatementObjectType.Agent), Some("John Doe"), Some(MBox("mailto:john.doe@example.com")), None, None, None)
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an agent (mbox_sha1sum)") {
          val actor: StatementActor = Agent(Some(StatementObjectType.Agent), Some("John Doe"), None, Some("2a6f4e470a1b9ef493f4ac83aa9456102a14f5c4"), None, None)
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Agent","name":"John Doe","mbox_sha1sum":"2a6f4e470a1b9ef493f4ac83aa9456102a14f5c4"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an agent (openId)") {
          val actor: StatementActor = Agent(Some(StatementObjectType.Agent), Some("John Doe"), None, None, Some("http://my.server.name/myname"), None)
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Agent","name":"John Doe","openid":"http://my.server.name/myname"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an agent (account)") {
          val actor: StatementActor = Agent(Some(StatementObjectType.Agent), Some("John Doe"), None, None, None, Some(Account("http://www.example.com", "123456")))
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Agent","name":"John Doe","account":{"homePage":"http://www.example.com","name":"123456"}}"""
          assert(encoded === expected)
        }
      }

      describe("[decoding]") {
        it("should successfully decode an agent (mbox)") {
          val data: String = """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = Agent(Some(StatementObjectType.Agent), Some("John Doe"), Some(MBox("mailto:john.doe@example.com")), None, None, None)
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode an agent (mbox_sha1sum)") {
          val data: String = """{"objectType":"Agent","name":"John Doe","mbox_sha1sum":"2a6f4e470a1b9ef493f4ac83aa9456102a14f5c4"}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = Agent(Some(StatementObjectType.Agent), Some("John Doe"), None, Some("2a6f4e470a1b9ef493f4ac83aa9456102a14f5c4"), None, None)
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode an agent (openId)") {
          val data: String = """{"objectType":"Agent","name":"John Doe","openid":"http://my.server.name/myname"}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = Agent(Some(StatementObjectType.Agent), Some("John Doe"), None, None, Some("http://my.server.name/myname"), None)
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode an agent (account)") {
          val data: String = """{"objectType":"Agent","name":"John Doe","account":{"homePage":"http://www.example.com","name":"123456"}}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = Agent(Some(StatementObjectType.Agent), Some("John Doe"), None, None, None, Some(Account("http://www.example.com", "123456")))
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode an agent without an explicit object type") {
          val data: String = """{"mbox":"mailto:john.doe@example.com"}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = Agent(None, None, Some(MBox("mailto:john.doe@example.com")), None, None, None)
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }
      }

    }

    describe("Group") {
      describe("[encoding]") {
        it("should successfully encode an identified group (mbox)") {
          val actor: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), Some(MBox("mailto:team.a@example.com")), None, None, None, None)
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an identified group (mbox_sha1sum)") {
          val actor: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), None, Some("5f129e82b8373086d1b517b823521f8186eca5fe"), None, None, None)
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Group","name":"Team A","mbox_sha1sum":"5f129e82b8373086d1b517b823521f8186eca5fe"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an identified group (openId)") {
          val actor: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), None, None, Some("http://my.server.name/team-a"), None, None)
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Group","name":"Team A","openid":"http://my.server.name/team-a"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an identified group (account)") {
          val actor: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), None, None, None, Some(Account("http://www.example.com", "123456")), None)
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Group","name":"Team A","account":{"homePage":"http://www.example.com","name":"123456"}}"""
          assert(encoded === expected)
        }

        it("should successfully encode an identified group with members") {
          val actor: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), Some(MBox("mailto:team.a@example.com")), None, None, None, Some(List(
            Agent(Some(StatementObjectType.Agent), Some("John Doe"), Some(MBox("mailto:john.doe@example.com")), None, None, None),
            Agent(Some(StatementObjectType.Agent), Some("Richard Roe"), Some(MBox("mailto:richard.roe@example.com")), None, None, None)
          )))
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com","member":[{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"},{"objectType":"Agent","name":"Richard Roe","mbox":"mailto:richard.roe@example.com"}]}"""
          assert(encoded === expected)
        }

        it("should successfully encode an anonymous group with members") {
          val actor: StatementActor = new Group(StatementObjectType.Group, None, None, None, None, None, Some(List(
            Agent(Some(StatementObjectType.Agent), Some("John Doe"), Some(MBox("mailto:john.doe@example.com")), None, None, None),
            Agent(Some(StatementObjectType.Agent), Some("Richard Roe"), Some(MBox("mailto:richard.roe@example.com")), None, None, None)
          )))
          val encoded: String = actor.asJson.noSpaces
          val expected: String = """{"objectType":"Group","member":[{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"},{"objectType":"Agent","name":"Richard Roe","mbox":"mailto:richard.roe@example.com"}]}"""
          assert(encoded === expected)
        }
      }

      describe("[decoding]") {
        it("should successfully decode an identified group (mbox)") {
          val data: String = """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com"}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), Some(MBox("mailto:team.a@example.com")), None, None, None, None)
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode an identified group (mbox_sha1sum)") {
          val data: String = """{"objectType":"Group","name":"Team A","mbox_sha1sum":"5f129e82b8373086d1b517b823521f8186eca5fe"}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), None, Some("5f129e82b8373086d1b517b823521f8186eca5fe"), None, None, None)
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode an identified group (openId)") {
          val data: String = """{"objectType":"Group","name":"Team A","openid":"http://my.server.name/team-a"}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), None, None, Some("http://my.server.name/team-a"), None, None)
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode an identified group (account)") {
          val data: String = """{"objectType":"Group","name":"Team A","account":{"homePage":"http://www.example.com","name":"123456"}}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), None, None, None, Some(Account("http://www.example.com", "123456")), None)
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode an identified group with members") {
          val data: String = """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com","member":[{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"},{"objectType":"Agent","name":"Richard Roe","mbox":"mailto:richard.roe@example.com"}]}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), Some(MBox("mailto:team.a@example.com")), None, None, None, Some(List(
            Agent(Some(StatementObjectType.Agent), Some("John Doe"), Some(MBox("mailto:john.doe@example.com")), None, None, None),
            Agent(Some(StatementObjectType.Agent), Some("Richard Roe"), Some(MBox("mailto:richard.roe@example.com")), None, None, None)
          )))
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode an anonymous group with members") {
          val data: String = """{"objectType":"Group","member":[{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"},{"objectType":"Agent","name":"Richard Roe","mbox":"mailto:richard.roe@example.com"}]}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = new Group(StatementObjectType.Group, None, None, None, None, None, Some(List(
            Agent(Some(StatementObjectType.Agent), Some("John Doe"), Some(MBox("mailto:john.doe@example.com")), None, None, None),
            Agent(Some(StatementObjectType.Agent), Some("Richard Roe"), Some(MBox("mailto:richard.roe@example.com")), None, None, None)
          )))
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }

        it("should successfully decode a group with members where the member object type is not declared") {
          val data: String = """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com","member":[{"mbox":"mailto:john.doe@example.com"},{"mbox":"mailto:richard.roe@example.com"}]}"""
          val decoded: Either[io.circe.Error, StatementActor] = decode[StatementActor](data)
          val expected: StatementActor = new Group(StatementObjectType.Group, Some("Team A"), Some(MBox("mailto:team.a@example.com")), None, None, None, Some(List(
            Agent(None, None, Some(MBox("mailto:john.doe@example.com")), None, None, None),
            Agent(None, None, Some(MBox("mailto:richard.roe@example.com")), None, None, None)
          )))
          println(decoded)
          decoded match {
            case Right(actual) => assert(actual === expected)
            case Left(err) => throw new Error(s"Decoding failed: $err")
          }
        }
      }
    }

    describe("[validation]") {
      it("should throw a statement validation exception when decoding an actor with an invalid openid identifier") {
        val data: String = """{"objectType":"Agent","name":"John Doe","openid":"my.server.name/myname"}"""
        assertThrows[StatementValidationException] {
          decode[StatementActor](data)
        }
      }
    }
  }
}
