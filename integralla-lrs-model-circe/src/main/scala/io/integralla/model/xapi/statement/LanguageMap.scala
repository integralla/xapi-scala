package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}

/** A language map is a dictionary where the key is a RFC 5646 Language Tag, and the value is a string in the language
  * specified in the tag.
  * @param value A map where both the keys and values are strings
  */
case class LanguageMap(value: Map[String, String])

object LanguageMap {
  implicit val encoder: Encoder[LanguageMap] = Encoder.encodeMap[String, String].contramap[LanguageMap](_.value)
  implicit val decoder: Decoder[LanguageMap] = Decoder.decodeMap[String, String].map[LanguageMap](LanguageMap.apply)
}
