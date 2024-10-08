package io.integralla.xapi.model

import io.circe.{Decoder, Encoder, Json}
import io.integralla.xapi.model.common.{Decodable, Encodable, Equivalence}

/** Extensions are defined by a map and logically relate to the part of the
  * Statement where they are present.
  *
  * @param value
  *   A map where each key must be a valid IRI, and each value a JSON data
  *   structure or value
  */
case class ExtensionMap(value: Map[IRI, Json]) extends Encodable[ExtensionMap] with Equivalence {

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * For extensions, the signature is computed first by concatenating the
    * signature of each IRI with a string representation of the JSON data
    * structure/value, sorted by keys and with no spaces. Then, that resulting
    * list of string values is sorted and hashed as usual
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      combine {
        value
          .map(ext => {
            combine(List(ext._1.signature(), ext._2.noSpacesSortKeys))
          }).toList.sorted
      }

    }
  }
}

object ExtensionMap extends Decodable[ExtensionMap] {
  implicit val encoder: Encoder[ExtensionMap] =
    Encoder.encodeMap[IRI, Json].contramap[ExtensionMap](_.value)
  implicit val decoder: Decoder[ExtensionMap] =
    Decoder.decodeMap[IRI, Json].map[ExtensionMap](ExtensionMap.apply)
}
