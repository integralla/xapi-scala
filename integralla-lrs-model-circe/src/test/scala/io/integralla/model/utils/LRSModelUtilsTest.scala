package io.integralla.model.utils

import io.integralla.model.xapi.statement.{Activity, Agent, StatementActor, StatementObjectType}
import io.integralla.model.xapi.statement.identifiers.{IRI, MBox}
import io.integralla.testing.spec.UnitSpec

class LRSModelUtilsTest extends UnitSpec {

  describe("LRSModelUtils") {
    describe("toJSON") {
      it("should encode a model object as json (activity)") {
        val activity: Activity = Activity(
          Some(StatementObjectType.Activity),
          IRI("https://lrs.integralla.io/xapi/activity/test"),
          None
        )
        val encoded: String = LRSModelUtils.toJSON(activity)
        assert(encoded === """{"objectType":"Activity","id":"https://lrs.integralla.io/xapi/activity/test"}""")
      }

      it("should encode a model object as json (actor)") {
        val actor: StatementActor = Agent(
          Some(StatementObjectType.Agent),
          Some("John Doe"),
          Some(MBox("mailto:john.doe@example.com")),
          None,
          None,
          None
        )
        val encoded: String = LRSModelUtils.toJSON(actor)
        val expected: String = """{"objectType":"Agent","name":"John Doe","mbox":"mailto:john.doe@example.com"}"""
        assert(encoded === expected)
      }
    }
  }

}
