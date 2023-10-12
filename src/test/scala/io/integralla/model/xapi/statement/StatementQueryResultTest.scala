package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.integralla.model.utils.LRSModelUtils
import io.integralla.model.xapi.identifiers.{IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import java.util.UUID

class StatementQueryResultTest extends UnitSpec {
  describe("StatementQueryResult") {
    describe("[encoding/decoding]") {
      it("should encode/decode a statement query result object") {
        val statements: StatementList = StatementList(
          List(
            Statement(
              id = Some(UUID.randomUUID()),
              actor = Agent(
                objectType = Some(StatementObjectType.Agent),
                name = Some("Lorum Ipsum"),
                mbox = Some(MBox("mailto:lorum.ipsum@integralla.io")),
                mbox_sha1sum = None,
                openid = None,
                account = None
              ),
              verb = StatementVerb(IRI("https://lrs.integralla.io/verbs/test"), None),
              `object` = StatementObject(
                Activity(Some(StatementObjectType.Activity), IRI("https://lrs.integralla.io/activity/test"), None)
              ),
              result = None,
              context = None,
              timestamp = None,
              stored = None,
              authority = None,
              version = None,
              attachments = None
            )
          )
        )
        val more: String = "/xapi/statements?exclusiveStart=8f232022-a4f6-4832-aea7-bc1650489bb3"
        val result: StatementQueryResult = StatementQueryResult(statements, Some(more))

        val encoded: String = LRSModelUtils.toJSON[StatementQueryResult](result)
        val decoded: StatementQueryResult = decode[StatementQueryResult](encoded).toOption.get

        assert(decoded === result)
      }
      it("should encode/decode a statement query result object without a more relative IRI") {
        val statements: StatementList = StatementList(
          List(
            Statement(
              id = Some(UUID.randomUUID()),
              actor = Agent(
                objectType = Some(StatementObjectType.Agent),
                name = Some("Lorum Ipsum"),
                mbox = Some(MBox("mailto:lorum.ipsum@integralla.io")),
                mbox_sha1sum = None,
                openid = None,
                account = None
              ),
              verb = StatementVerb(IRI("https://lrs.integralla.io/verbs/test"), None),
              `object` = StatementObject(
                Activity(Some(StatementObjectType.Activity), IRI("https://lrs.integralla.io/activity/test"), None)
              ),
              result = None,
              context = None,
              timestamp = None,
              stored = None,
              authority = None,
              version = None,
              attachments = None
            )
          )
        )
        val result: StatementQueryResult = StatementQueryResult(statements, None)

        val encoded: String = LRSModelUtils.toJSON[StatementQueryResult](result)
        val decoded: StatementQueryResult = decode[StatementQueryResult](encoded).toOption.get

        assert(!encoded.contains("more"))
        assert(decoded === result)
      }
    }
  }
}
