package io.integralla.xapi.model

import org.scalatest.funspec.AnyFunSpec

import java.util.UUID
import scala.util.Try

class StatementQueryResultTest extends AnyFunSpec {
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
                Activity(
                  Some(StatementObjectType.Activity),
                  IRI("https://lrs.integralla.io/activity/test"),
                  None
                )
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

        val encoded: String = result.toJson()
        val decoded: Try[StatementQueryResult] = StatementQueryResult(encoded)

        assert(decoded.isSuccess)
        assert(decoded.get === result)
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
                Activity(
                  Some(StatementObjectType.Activity),
                  IRI("https://lrs.integralla.io/activity/test"),
                  None
                )
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

        val encoded: String = result.toJson()
        val decoded: Try[StatementQueryResult] = StatementQueryResult(encoded)

        assert(decoded.isSuccess)
        assert(!encoded.contains("more"))
        assert(decoded.get === result)
      }
    }
  }
}
