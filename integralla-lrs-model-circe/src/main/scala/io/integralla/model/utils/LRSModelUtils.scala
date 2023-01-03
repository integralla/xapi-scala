package io.integralla.model.utils

import io.circe.Encoder
import io.circe.syntax.EncoderOps

/** LRS Model Utilities */
object LRSModelUtils {

  /** Encodes an LRS Model object as JSON
    *
    * @param instance An instance of a model class
    * @param spaces Whether to include newlines and indentation in the output
    * @param encoder Implicit encoder
    * @tparam A The model type
    * @return A JSON encoded string
    */
  def toJSON[A](instance: A, spaces: Boolean = false)(implicit encoder: Encoder[A]): String = {
    if (spaces) {
      instance.asJson.spaces2
    } else {
      instance.asJson.noSpaces
    }
  }
}
