package io.integralla.xapi.model

import org.scalatest.funspec.AnyFunSpec

import scala.util.Try

class XApiVersionTest extends AnyFunSpec {
  describe("XApiVersion") {
    describe("[encoding/decoding]") {
      it("should encode/decode a valid version (1.0.3)") {
        val version: XApiVersion = XApiVersion(1, 0, Some(3))
        val encoded: String = version.toJson()
        assert(encoded === """"1.0.3"""")

        val decoded: Try[XApiVersion] = XApiVersion(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === version)
      }
      it("should encode/decode a valid version (1.0.9)") {
        val version: XApiVersion = XApiVersion(1, 0, Some(9))
        val encoded: String = version.toJson()
        assert(encoded === """"1.0.9"""")

        val decoded: Try[XApiVersion] = XApiVersion(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === version)
      }
      it("should encode/decode a valid version (2.0.0)") {
        val version: XApiVersion = XApiVersion(2, 0, Some(0))
        val encoded: String = version.toJson()
        assert(encoded === """"2.0.0"""")

        val decoded: Try[XApiVersion] = XApiVersion(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === version)
      }
      it("should encode/decode a valid version without a patch (1.0)") {
        val version: XApiVersion = XApiVersion(1, 0, None)
        val encoded: String = version.toJson()
        assert(encoded === """"1.0"""")

        val decoded: Try[XApiVersion] = XApiVersion(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === version)
      }
      it("should encode/decode a valid version without a patch (2.0)") {
        val version: XApiVersion = XApiVersion(2, 0, None)
        val encoded: String = version.toJson()
        assert(encoded === """"2.0"""")

        val decoded: Try[XApiVersion] = XApiVersion(encoded)
        assert(decoded.isSuccess)
        assert(decoded.get === version)
      }
      it("should return an exception when decoding an unsupported version (0.9.9)") {
        val encoded: String = """"0.9.9""""
        val exception = intercept[Throwable] { XApiVersion(encoded).get }
        assert(exception.getMessage.contains("Unsupported xAPI version"))
      }
      it("should return an exception when decoding an unsupported version (1.1.0)") {
        val encoded: String = """"1.1.0""""
        val exception = intercept[Throwable] {
          XApiVersion(encoded).get
        }
        assert(exception.getMessage.contains("Unsupported xAPI version"))
      }
      it("should return an exception when decoding an unsupported version (2.1.0)") {
        val encoded: String = """"2.1.0""""
        val exception = intercept[Throwable] {
          XApiVersion(encoded).get
        }
        assert(exception.getMessage.contains("Unsupported xAPI version"))
      }
    }
  }
}
