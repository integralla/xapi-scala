package io.integralla.model.utils

import io.circe.{Decoder, Encoder, Json}
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.exceptions.LRSModelDecodingException

import scala.reflect.{classTag, ClassTag}
import scala.util.{Failure, Success, Try}

/** Trait for LRS Models which provides methods for encoding/decoding */
trait LRSModel {

  /** Encode the model as a JSON string
    *
    * @param spaces
    *   Whether to include indentation in the encoded object
    * @param sorted
    *   Whether to sort the values in the encoded object
    * @param encoder
    *   Implicit encoder
    * @tparam A
    *   The model's type
    * @return
    *   A JSON encoded string representation of the model
    */
  def toJson[A](spaces: Boolean = false, sorted: Boolean = false)(implicit
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

object LRSModel {

  /** An apply method to generate an instance of class that extends this trait
    * from a JSON encoded object
    *
    * @param json
    *   The JSON encoded object
    * @param decoder
    *   Implicit decoder
    * @tparam A
    *   The model's type
    * @return
    *   An instance of the model
    */
  def apply[A: ClassTag](json: String)(implicit decoder: Decoder[A]): Try[A] =
    decode[A](json) match {
      case Left(exception) =>
        Failure(
          new LRSModelDecodingException(
            s"Unable to decode json representation into type ${classTag[A].runtimeClass}: $exception"
          )
        )
      case Right(value) => Success(value)
    }

}
