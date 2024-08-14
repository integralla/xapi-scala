package io.integralla.xapi.model

import io.integralla.xapi.model.exceptions.StatementValidationException
import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

class StatementActorTest extends AnyFunSpec {

  describe("An Actor") {

    describe("Agent") {
      describe("[encoding]") {
        it("should successfully encode an agent (mbox)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox = Some(MBox("mailto:john.doe@example.com"))
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an agent (mbox_sha1sum)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox_sha1sum = Some("2a6f4e470a1b9ef493f4ac83aa9456102a14f5c4")
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Agent","name":"John Doe","mbox_sha1sum":"2a6f4e470a1b9ef493f4ac83aa9456102a14f5c4"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an agent (openId)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            openid = Some("http://my.server.name/myname")
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Agent","name":"John Doe","openid":"http://my.server.name/myname"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an agent (account)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            account = Some(Account("http://www.example.com", "123456"))
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Agent","name":"John Doe","account":{"homePage":"http://www.example.com","name":"123456"}}"""
          assert(encoded === expected)
        }

        it("should successfully encode an agent as an Agent instance") {
          val actor: Agent = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox = Some(MBox("mailto:john.doe@example.com"))
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
          assert(encoded === expected)
        }
      }

      describe("[decoding]") {
        it("should successfully decode an agent (mbox)") {
          val data: String =
            """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox = Some(MBox("mailto:john.doe@example.com"))
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an agent (mbox_sha1sum)") {
          val data: String =
            """{"objectType":"Agent","name":"John Doe","mbox_sha1sum":"2a6f4e470a1b9ef493f4ac83aa9456102a14f5c4"}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox_sha1sum = Some("2a6f4e470a1b9ef493f4ac83aa9456102a14f5c4")
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an agent (openId)") {
          val data: String =
            """{"objectType":"Agent","name":"John Doe","openid":"http://my.server.name/myname"}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            openid = Some("http://my.server.name/myname")
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an agent (account)") {
          val data: String =
            """{"objectType":"Agent","name":"John Doe","account":{"homePage":"http://www.example.com","name":"123456"}}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            account = Some(Account("http://www.example.com", "123456"))
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an agent without an explicit object type") {
          val data: String = """{"mbox":"mailto:john.doe@example.com"}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = Agent(mbox = Some(MBox("mailto:john.doe@example.com")))

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an agent as an Agent instance") {
          val data: String =
            """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
          val decoded: Try[Agent] = Agent(data)
          val expected: Agent = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox = Some(MBox("mailto:john.doe@example.com"))
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }
      }

      describe("[equivalence]") {

        val common: Agent = Agent(
          objectType = Some(StatementObjectType.Agent),
          name = Some("Populus Tremuloides"),
          mbox = Some(MBox("mailto:populus.tremuloides@integralla.io"))
        )

        it("should return true when both instances are logically equivalent [mbox]") {
          val left = common.copy()
          val right = common.copy()
          assert(left.isEquivalentTo(right))
        }

        it("should return true when both instances are logically equivalent [mbox_sha1sum]") {
          val left = common.copy(
            mbox = None,
            mbox_sha1sum = Some("b7d8faae0f425985a6e170ed452bf60fb7033758")
          )
          val right = common.copy(
            mbox = None,
            mbox_sha1sum = Some("b7d8faae0f425985a6e170ed452bf60fb7033758")
          )
          assert(left.isEquivalentTo(right))
        }

        it("should return true when both instances are logically equivalent [openid]") {
          val left = common.copy(
            mbox = None,
            openid = Some("https://lrs.integralla.io/openid/populus.tremuloides")
          )
          val right = common.copy(
            mbox = None,
            openid = Some("https://lrs.integralla.io/openid/populus.tremuloides")
          )
          assert(left.isEquivalentTo(right))
        }

        it("should return true when both instances are logically equivalent [account]") {
          val left =
            common.copy(
              mbox = None,
              account = Some(Account("https://lrs.integralla.io/id/", "populus.tremuloides"))
            )
          val right =
            common.copy(
              mbox = None,
              account = Some(Account("https://lrs.integralla.io/id/", "populus.tremuloides"))
            )
          assert(left.isEquivalentTo(right))
        }

        it(
          "should return true when both instances are logically equivalent, excepting object type definition"
        ) {
          val left = common.copy()
          val right = common.copy(objectType = None)
          assert(left.isEquivalentTo(right))
        }

        it(
          "should return true when both instances are logically equivalent, excepting case sensitivity"
        ) {
          val left = common.copy()
          val right = common.copy(mbox = Some(MBox("MAILTO:POPULUS.TREMULOIDES@INTEGRALLA.IO")))
          assert(left.isEquivalentTo(right))
        }

        it(
          "should return true when both instances are logically equivalent, excepting case sensitivity [IRI case sensitivity]"
        ) {
          val left =
            common.copy(
              mbox = None,
              account = Some(Account("https://lrs.integralla.io/id/", "populus.tremuloides"))
            )
          val right =
            common.copy(
              mbox = None,
              account = Some(Account("https://LRS.INTEGRALLA.IO/id/", "populus.tremuloides"))
            )
          assert(left.isEquivalentTo(right))
        }

        it(
          "should return false when both instances are not logically equivalent [different value]"
        ) {
          val left = common.copy()
          val right = common.copy(name = Some("Quaking Aspen"))
          assert(left.isEquivalentTo(right) === false)
        }

        it("should return false when both instances are not logically equivalent [no value]") {
          val left = common.copy()
          val right = common.copy(name = None)
          assert(left.isEquivalentTo(right) === false)
        }
      }

    }

    describe("Group") {
      describe("[encoding]") {
        it("should successfully encode an identified group (mbox)") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox = Some(MBox("mailto:team.a@example.com"))
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an identified group (mbox_sha1sum)") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox_sha1sum = Some("5f129e82b8373086d1b517b823521f8186eca5fe")
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Group","name":"Team A","mbox_sha1sum":"5f129e82b8373086d1b517b823521f8186eca5fe"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an identified group (openId)") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            openid = Some("http://my.server.name/team-a")
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Group","name":"Team A","openid":"http://my.server.name/team-a"}"""
          assert(encoded === expected)
        }

        it("should successfully encode an identified group (account)") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            account = Some(Account("http://www.example.com", "123456"))
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Group","name":"Team A","account":{"homePage":"http://www.example.com","name":"123456"}}"""
          assert(encoded === expected)
        }

        it("should successfully encode an identified group with members") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox = Some(MBox("mailto:team.a@example.com")),
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                ),
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("Richard Roe"),
                  mbox = Some(MBox("mailto:richard.roe@example.com"))
                )
              )
            )
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com","member":[{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"},{"objectType":"Agent","name":"Richard Roe","mbox":"mailto:richard.roe@example.com"}]}"""
          assert(encoded === expected)
        }

        it("should successfully encode an anonymous group with members") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                ),
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("Richard Roe"),
                  mbox = Some(MBox("mailto:richard.roe@example.com"))
                )
              )
            )
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Group","member":[{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"},{"objectType":"Agent","name":"Richard Roe","mbox":"mailto:richard.roe@example.com"}]}"""
          assert(encoded === expected)
        }

        it("should successfully encode an identified group as a Group") {
          val actor: Group = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox = Some(MBox("mailto:team.a@example.com"))
          )
          val encoded: String = actor.toJson()
          val expected: String =
            """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com"}"""
          assert(encoded === expected)
        }
      }

      describe("[decoding]") {
        it("should successfully decode an identified group (mbox)") {
          val data: String =
            """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com"}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox = Some(MBox("mailto:team.a@example.com"))
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an identified group (mbox_sha1sum)") {
          val data: String =
            """{"objectType":"Group","name":"Team A","mbox_sha1sum":"5f129e82b8373086d1b517b823521f8186eca5fe"}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox_sha1sum = Some("5f129e82b8373086d1b517b823521f8186eca5fe")
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an identified group (openId)") {
          val data: String =
            """{"objectType":"Group","name":"Team A","openid":"http://my.server.name/team-a"}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            openid = Some("http://my.server.name/team-a")
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an identified group (account)") {
          val data: String =
            """{"objectType":"Group","name":"Team A","account":{"homePage":"http://www.example.com","name":"123456"}}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            account = Some(Account("http://www.example.com", "123456"))
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an identified group with members") {
          val data: String =
            """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com","member":[{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"},{"objectType":"Agent","name":"Richard Roe","mbox":"mailto:richard.roe@example.com"}]}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox = Some(MBox("mailto:team.a@example.com")),
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                ),
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("Richard Roe"),
                  mbox = Some(MBox("mailto:richard.roe@example.com"))
                )
              )
            )
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an anonymous group with members") {
          val data: String =
            """{"objectType":"Group","member":[{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"},{"objectType":"Agent","name":"Richard Roe","mbox":"mailto:richard.roe@example.com"}]}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                ),
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("Richard Roe"),
                  mbox = Some(MBox("mailto:richard.roe@example.com"))
                )
              )
            )
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it(
          "should successfully decode a group with members where the member object type is not declared"
        ) {
          val data: String =
            """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com","member":[{"mbox":"mailto:john.doe@example.com"},{"mbox":"mailto:richard.roe@example.com"}]}"""
          val decoded: Try[StatementActor] = StatementActor(data)
          val expected: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox = Some(MBox("mailto:team.a@example.com")),
            member = Some(
              List(
                Agent(mbox = Some(MBox("mailto:john.doe@example.com"))),
                Agent(mbox = Some(MBox("mailto:richard.roe@example.com")))
              )
            )
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }

        it("should successfully decode an identified group as a Group") {
          val data: String =
            """{"objectType":"Group","name":"Team A","mbox":"mailto:team.a@example.com"}"""
          val decoded: Try[Group] = Group(data)
          val expected: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox = Some(MBox("mailto:team.a@example.com"))
          )

          assert(decoded.isSuccess)
          assert(decoded.get === expected)
        }
      }

      describe("[equivalence]") {

        val common: Group = new Group(
          objectType = StatementObjectType.Group,
          name = Some("Team A"),
          mbox = Some(MBox("mailto:team.a@integralla.io")),
          member = Some(
            List(
              Agent(mbox = Some(MBox("mailto:elaeagnus.angustifolia@integralla.io"))),
              Agent(mbox = Some(MBox("mailto:fraxinus.americana@integralla.io")))
            )
          )
        )
        it("should return true when both instances are logically equivalent [identified group]") {
          val left = common.copy()
          val right = common.copy()
          assert(left.isEquivalentTo(right))
        }

        it("should return true when both instances are logically equivalent [anonymous group]") {
          val left = common.copy(mbox = None)
          val right = common.copy(mbox = None)
          assert(left.isEquivalentTo(right))
        }
        it(
          "should return false when both instances are not logically equivalent [different name]"
        ) {
          val left = common.copy()
          val right = common.copy(name = Some("Team Alpha"))
          assert(left.isEquivalentTo(right) === false)
        }
        it(
          "should return false when both instances are not logically equivalent [different member set]"
        ) {
          val left = common.copy()
          val right = common.copy(member =
            Some(
              List(
                Agent(mbox = Some(MBox("mailto:elaeagnus.angustifolia@integralla.io"))),
                Agent(mbox = Some(MBox("mailto:fraxinus.pennsylvanica@integralla.io")))
              )
            )
          )
          assert(left.isEquivalentTo(right) === false)
        }
        it(
          "should return false when both instances are not logically equivalent [different member agent signature]"
        ) {
          val left = common.copy()
          val right = common.copy(member =
            Some(
              List(
                Agent(mbox = Some(MBox("mailto:elaeagnus.angustifolia@integralla.io"))),
                Agent(
                  name = Some("Green Ash"),
                  mbox = Some(MBox("mailto:fraxinus.americana@integralla.io"))
                )
              )
            )
          )
          assert(left.isEquivalentTo(right) === false)
        }
      }

      describe("isAnonymous") {
        it("should return true for an anonymous group") {
          val group = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                )
              )
            )
          )
          assert(group.isAnonymous === true)
        }
        it("should return false for an identified group") {
          val group = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox = Some(MBox("mailto:team-a@example.com")),
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                )
              )
            )
          )
          assert(group.isAnonymous === false)
        }
      }
    }

    describe("[common]") {
      describe("[validation]") {
        it(
          "should throw a statement validation exception when decoding an actor with an invalid openid identifier"
        ) {
          val data: String =
            """{"objectType":"Agent","name":"John Doe","openid":"my.server.name/myname"}"""
          assertThrows[StatementValidationException] {
            StatementActor(data).get
          }
        }
      }

      describe("[equivalence]") {

        val agent = Agent(
          objectType = Some(StatementObjectType.Agent),
          name = Some("Populus Tremuloides"),
          mbox = Some(MBox("mailto:populus.tremuloides@integralla.io"))
        )

        val group = new Group(
          objectType = StatementObjectType.Group,
          name = Some("Team A"),
          mbox = Some(MBox("mailto:team.a@integralla.io")),
          member = Some(
            List(
              Agent(mbox = Some(MBox("mailto:elaeagnus.angustifolia@integralla.io"))),
              Agent(mbox = Some(MBox("mailto:fraxinus.americana@integralla.io")))
            )
          )
        )

        it("should test for equivalence between actors that are agents") {
          val left = agent.copy().asInstanceOf[StatementActor]
          val right = agent.copy().asInstanceOf[StatementActor]
          assert(left.isEquivalentTo(right))
        }

        it("should test for equivalence between  actors that are groups") {
          val left = group.copy().asInstanceOf[StatementActor]
          val right = group.copy().asInstanceOf[StatementActor]
          assert(left.isEquivalentTo(right))
        }

        it("should test for equivalence between an agent to a group") {
          val left = agent.copy().asInstanceOf[StatementActor]
          val right = group.copy().asInstanceOf[StatementActor]
          assert(left.isEquivalentTo(right) === false)
        }
      }

      describe("ifiType()") {
        it("should return the type of ifi (mbox)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox = Some(MBox("mailto:john.doe@example.com"))
          )
          assert(actor.ifiType().get === "mbox")
        }
        it("should return the type of ifi (mbox_sha1sum)") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox_sha1sum = Some("5f129e82b8373086d1b517b823521f8186eca5fe")
          )
          assert(actor.ifiType().get === "mbox_sha1sum")
        }
        it("should return the type of ifi (openid)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            openid = Some("http://my.server.name/myname")
          )
          assert(actor.ifiType().get === "openid")
        }
        it("should return the type of ifi (account)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            account = Some(Account("http://www.example.com", "123456"))
          )
          assert(actor.ifiType().get === "account")
        }
        it("should return none in the case of an anonymous group") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                )
              )
            )
          )
          assert(actor.ifiType().isEmpty)
        }
      }

      describe("ifiValue()") {
        it("should return the type of ifi value (mbox)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox = Some(MBox("mailto:john.doe@example.com"))
          )
          assert(actor.ifiValue().get === "mailto:john.doe@example.com")
        }
        it("should return the type of ifi value (mbox_sha1sum)") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox_sha1sum = Some("5f129e82b8373086d1b517b823521f8186eca5fe")
          )
          assert(actor.ifiValue().get === "5f129e82b8373086d1b517b823521f8186eca5fe")
        }
        it("should return the type of ifi value (openid)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            openid = Some("http://my.server.name/myname")
          )
          assert(actor.ifiValue().get === "http://my.server.name/myname")
        }
        it("should return the type of ifi value (account)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            account = Some(Account("http://www.example.com", "123456"))
          )
          assert(actor.ifiValue().get === "http://www.example.com#123456")
        }
        it("should return none in the case of an anonymous group") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                )
              )
            )
          )
          assert(actor.ifiValue().isEmpty)
        }
      }

      describe("ifiKey()") {
        it("should return the type of ifi value (mbox)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox = Some(MBox("mailto:john.doe@example.com"))
          )
          assert(actor.ifiKey().get === "mbox#mailto:john.doe@example.com")
        }
        it("should return the type of ifi value (mbox_sha1sum)") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            mbox_sha1sum = Some("5f129e82b8373086d1b517b823521f8186eca5fe")
          )
          assert(actor.ifiKey().get === "mbox_sha1sum#5f129e82b8373086d1b517b823521f8186eca5fe")
        }
        it("should return the type of ifi value (openid)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            openid = Some("http://my.server.name/myname")
          )
          assert(actor.ifiKey().get === "openid#http://my.server.name/myname")
        }
        it("should return the type of ifi value (account)") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            account = Some(Account("http://www.example.com", "123456"))
          )
          assert(actor.ifiKey().get === "account#http://www.example.com#123456")
        }
        it("should return none in the case of an anonymous group") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                )
              )
            )
          )
          assert(actor.ifiKey().isEmpty)
        }
      }

      describe("actorType") {
        it("should return the statement object type for an agent") {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox = Some(MBox("mailto:john.doe@example.com"))
          )
          assert(actor.actorType() === StatementObjectType.Agent)
        }
        it(
          "should return the statement object type for an agent when the type has not been explicitly set"
        ) {
          val actor: StatementActor = Agent(
            objectType = Some(StatementObjectType.Agent),
            name = Some("John Doe"),
            mbox = Some(MBox("mailto:john.doe@example.com"))
          )
          assert(actor.actorType() === StatementObjectType.Agent)
        }
        it("should return the statement object type for a group") {
          val actor: StatementActor = new Group(
            objectType = StatementObjectType.Group,
            name = Some("Team A"),
            member = Some(
              List(
                Agent(
                  objectType = Some(StatementObjectType.Agent),
                  name = Some("John Doe"),
                  mbox = Some(MBox("mailto:john.doe@example.com"))
                )
              )
            )
          )
          assert(actor.actorType() === StatementObjectType.Group)
        }
      }

      describe("asList") {

        val agent: Agent = Agent(
          objectType = Some(StatementObjectType.Agent),
          name = Some("Populus Tremuloides"),
          mbox = Some(MBox("mailto:populus.tremuloides@integralla.io"))
        )

        val group: Group = Group(
          objectType = StatementObjectType.Group,
          name = Some("Team A"),
          mbox = Some(MBox("mailto:team-a@integralla.io")),
          member = Some(
            List(agent)
          )
        )

        it("should return a list with a single actor if the actor is an agent") {
          val actor: StatementActor = agent.copy()
          val identities = actor.asList()
          assert(identities.nonEmpty)
          assert(identities.length === 1)
        }

        it(
          "should return a list with a single actor if the actor is an identified group with no members"
        ) {
          val actor: StatementActor = group.copy(member = None)
          val identities = actor.asList()
          assert(identities.nonEmpty)
          assert(identities.length === 1)
        }
        it(
          "should return a list with the group and all members if the actor is an identified group with members"
        ) {
          val actor: StatementActor = group.copy()
          val identities = actor.asList()
          assert(identities.nonEmpty)
          assert(identities.length === 2)
          val team = identities.find(_._1.mbox.get.value === "mailto:team-a@integralla.io").get
          assert(team._2 === false)

          val member =
            identities.find(_._1.mbox.get.value === "mailto:populus.tremuloides@integralla.io").get
          assert(member._2 === true)
        }
        it("should return a list with all members if the actor is an anonymous group") {
          val actor: StatementActor = group.copy(mbox = None)
          val identities = actor.asList()
          assert(identities.nonEmpty)
          assert(identities.length === 1)

          assert(identities.head._1.mbox.get.value === "mailto:populus.tremuloides@integralla.io")
          assert(identities.head._2 === true)
        }
      }
    }
  }
}
