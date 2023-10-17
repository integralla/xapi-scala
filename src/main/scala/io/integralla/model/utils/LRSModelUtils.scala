package io.integralla.model.utils

import io.circe.{jawn, Decoder, Encoder}
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.exceptions.LRSModelDecodingException

import scala.reflect.{classTag, ClassTag}
import scala.util.{Failure, Success, Try}

/** LRS Model Utilities */
object LRSModelUtils {

  /** Encodes an LRS Model object as JSON
    *
    * @param instance
    *   An instance of a model class
    * @param spaces
    *   Whether to include newlines and indentation in the output
    * @param encoder
    *   Implicit encoder
    * @tparam A
    *   The model type
    * @return
    *   A JSON encoded string
    */
  def toJSON[A](instance: A, spaces: Boolean = false)(implicit encoder: Encoder[A]): String = {
    if (spaces) {
      instance.asJson.spaces2
    } else {
      instance.asJson.noSpaces
    }
  }

  /** Decodes a JSON encoded string into the specified type
    * @param json
    *   The JSON encoded string to decode
    * @param decoder
    *   Implicit decoder
    * @tparam A
    *   The model type to decode into
    * @return
    *   An instance of the specified model on success, else an exception
    */
  def fromJSON[A: ClassTag](json: String)(implicit decoder: Decoder[A]): Try[A] = {
    jawn.decode[A](json) match {
      case Left(exception) =>
        Failure(
          new LRSModelDecodingException(s"Unable to decode string into type ${classTag[A].runtimeClass}: $exception")
        )
      case Right(value) => Success(value)
    }
  }
}
