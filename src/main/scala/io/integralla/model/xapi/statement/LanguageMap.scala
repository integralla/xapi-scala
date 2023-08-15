package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.common.Equivalence

import java.util.Locale
import scala.jdk.CollectionConverters.*

/** A language map is a dictionary where the key is a RFC 5646 Language Tag, and the value is a string in the language
  * specified in the tag.
  * @param value A map where both the keys and values are strings
  */
case class LanguageMap(value: Map[String, String]) extends Equivalence {

  /** @return The first item in the language map if the map is non-empty, else none */
  def first: Option[LanguageMap] =
    if (value.nonEmpty) {
      Some(LanguageMap(Map(value.head._1 -> value.head._2)))
    } else None

  /** @param tag RFC 5646 Language Tag
    * @return The matching item in the language map if found, else none
    */
  def lookup(tag: String): Option[LanguageMap] =
    value.get(tag).map(value => LanguageMap(Map(tag -> value)))

  /** Produces a language map with a single item
    *
    * If the Language Priority List is non-empty, the method will attempt to lookup the
    * best-matching language tag using the lookup mechanism defined in RFC 4647 and, if
    * successful return the language map entry corresponding to it.
    *
    * If the Language Priority List is empty, or if no match is found, the method will
    * return the first entry in the language map.
    *
    * @param priorityList Language Priority List
    * @return LanguageMap with a single item
    */
  def preferred(priorityList: List[Locale.LanguageRange]): Option[LanguageMap] =
    if (priorityList.nonEmpty) {
      Option(Locale.lookupTag(priorityList.asJava, value.keys.toList.asJava)) match
        case Some(tag) => lookup(tag)
        case None      => first
    } else first

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
