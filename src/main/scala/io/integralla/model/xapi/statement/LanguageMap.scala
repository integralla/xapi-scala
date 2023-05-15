package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.common.Equivalence

/** A language map is a dictionary where the key is a RFC 5646 Language Tag, and the value is a string in the language
  * specified in the tag.
  * @param value A map where both the keys and values are strings
  */
case class LanguageMap(value: Map[String, String]) extends Equivalence {

  /** Generates a signature that can be used to test logical equivalence between objects
    *
    * The signature for a language map is computed by creating a new list of strings, each composed by the concatenation
    * of the key (language code) and value with a standard separator. Per RFC 5646, language codes are case insensitive
    * and therefore, for comparison purposes, it is converted to lower case. This list of strings is then concatenated
    * and hashed as usual to generate the signature.
    *
    * @return A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      combine {
        value
          .map(languageMap => {
            combine(List(lower(languageMap._1), languageMap._2))
          }).toList.sorted
      }
    }
  }
}

object LanguageMap {
  implicit val encoder: Encoder[LanguageMap] = Encoder.encodeMap[String, String].contramap[LanguageMap](_.value)
  implicit val decoder: Decoder[LanguageMap] = Decoder.decodeMap[String, String].map[LanguageMap](LanguageMap.apply)
}
