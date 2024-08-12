package io.integralla.xapi.model.common

import io.circe.{Encoder, Json}
import io.circe.syntax.EncoderOps

/** Trait to be extended by the case class for each model
  *
  * @tparam A
  *   Model type
  */
trait Encodable[A] {

  /** Encode the model as a JSON string
    *
    * @param spaces
    *   Whether to include indentation in the encoded object
    * @param sorted
    *   Whether to sort the values in the encoded object
    * @param encoder
    *   Implicit encoder
    * @return
    *   A JSON encoded string representation of the model
    */
  def toJson(spaces: Boolean = false, sorted: Boolean = false)(implicit
    encoder: Encoder[A]
  ): String = {
    val encoded: Json = this.asInstanceOf[A].asJson.dropNullValues
    (spaces, sorted) match {
      case (true, true)   => encoded.spaces2SortKeys
      case (true, false)  => encoded.spaces2
      case (false, true)  => encoded.noSpacesSortKeys
      case (false, false) => encoded.noSpaces
    }
  }

}
