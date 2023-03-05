package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.testing.spec.UnitSpec

import java.util.UUID

class StatementRefTest extends UnitSpec {

  describe("StatementRef") {
    describe("[encoding]") {
      it("should successfully encode a statement reference") {
        val ref: StatementRef =
          StatementRef(StatementObjectType.StatementRef, UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2"))
        val actual = ref.asJson.noSpaces
        val expected = """{"objectType":"StatementRef","id":"7cf5941a-9631-4741-83eb-28beb8ff28e2"}"""
        assert(actual === expected)
      }
    }
    describe("[decoding]") {
      it("should successfully decode a statement reference") {
        val data: String = """{"objectType":"StatementRef","id":"7cf5941a-9631-4741-83eb-28beb8ff28e2"}"""
        val decoded: Either[io.circe.Error, StatementRef] = decode[StatementRef](data)
        val expected: StatementRef =
          StatementRef(StatementObjectType.StatementRef, UUID.fromString("7cf5941a-9631-4741-83eb-28beb8ff28e2"))
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
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
