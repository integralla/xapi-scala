package io.integralla.xapi.model

import org.scalatest.funspec.AnyFunSpec

import java.util.UUID

class StatementRefTest extends AnyFunSpec {

  describe("StatementRef") {
    describe("[encoding]") {
      it("should successfully encode a statement reference") {
        val ref: StatementRef =
          StatementRef(
            StatementObjectType.StatementRef,
            UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2")
          )
        val actual = ref.toJson()
        val expected =
          """{"objectType":"StatementRef","id":"7cf5941a-9631-4741-83eb-28beb8ff28e2"}"""
        assert(actual === expected)
      }
    }
    describe("[decoding]") {
      it("should successfully decode a statement reference") {
        val data: String =
          """{"objectType":"StatementRef","id":"7cf5941a-9631-4741-83eb-28beb8ff28e2"}"""
        val decoded = StatementRef(data)
        val expected: StatementRef =
          StatementRef(
            StatementObjectType.StatementRef,
            UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2")
          )

        assert(decoded.isSuccess)
        assert(decoded.get === expected)

      }
    }

    describe("[equivalence]") {
      it("should return true if both objects are equivalent") {
        val left: StatementRef = StatementRef(
          StatementObjectType.StatementRef,
          UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2")
        )
        val right: StatementRef = left.copy()
        assert(left.isEquivalentTo(right))
      }
      it("should return false if both objects are not equivalent") {
        val left: StatementRef = StatementRef(
          StatementObjectType.StatementRef,
          UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2")
        )
        val right: StatementRef = left.copy(id = UUID.randomUUID())
        assert(left.isEquivalentTo(right) === false)
      }
    }
  }
}
