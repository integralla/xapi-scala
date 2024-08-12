package io.integralla.xapi.model.common

import io.circe.Decoder
import io.circe.parser.decode
import io.integralla.xapi.model.exceptions.ModelDecodingException

import scala.util.{Failure, Success, Try}

/** Trait to be extended by a companion object for each model
  *
  * @tparam A
  *   Model type
  */
trait Decodable[A] {

  /** Decodes a JSON encoded string into the specified type
    *
    * @param json
    *   The JSON encoded string to decode
    * @param decoder
    *   Implicit decoder
    * @return
    *   An instance of the specified model on success, else an exception
    */
  def fromJson(json: String)(implicit decoder: Decoder[A]): Try[A] = {
    decode[A](json) match {
      case Left(exception) =>
        Failure(
          new ModelDecodingException(
            s"Unable to decode json representation into type ${this.getClass.getSimpleName}: $exception"
          )
        )
      case Right(value) => Success(value)
    }
  }

  /** Apply method which can be used to create an instance of the specified type
    * from a JSON encoded instance
    *
    * @param json
    *   The JSON encoded string to decode
    * @param decoder
    *   Implicit decoder
    * @return
    *   An instance of the specified model on success, else an exception
    */
  def apply(json: String)(implicit decoder: Decoder[A]): Try[A] =
    fromJson(json)

}
