package io.integralla.model.xapi.common

import io.circe.Encoder

import java.time.format.DateTimeFormatter
import java.time.OffsetDateTime

/** Custom encoders */
object CustomEncoders {

  /** Encodes a timestamp with nanosecond precision */
  implicit val timestampEncoder: Encoder[OffsetDateTime] = Encoder.encodeString
    .contramap[OffsetDateTime](
      _.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnnXXX"))
    )

}
