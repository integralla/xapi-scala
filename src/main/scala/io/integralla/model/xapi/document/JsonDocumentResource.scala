package io.integralla.model.xapi.document

import com.typesafe.scalalogging.StrictLogging
import io.circe.Json
import io.circe.parser.*
import io.integralla.model.exceptions.JsonObjectValidationException

import scala.util.{Failure, Success, Try}

/** Represents an xAPI Document Resource of the type application/json
  *
  * @param json
  *   JSON object
  */
case class JsonDocumentResource(json: Json) {

  /** Merges the JSON of the current document resource with that of another
    *
    * Only the top-level properties of the documents are merged (as required by
    * the xAPI specification), and no attempt is made to preserve type.
    *
    * @param that
    *   Another JSON document resource object to merge with the current instance
    * @return
    *   JSON Document Resource object
    */
  def merge(that: JsonDocumentResource): JsonDocumentResource = {
    (json.asObject, that.json.asObject) match
      case (Some(left), Some(right)) =>
        new JsonDocumentResource(
          Json.fromJsonObject {
            left.toIterable.foldLeft(right) { case (acc, (key, value)) =>
              right(key).fold(acc.add(key, value)) { r => acc.add(key, r) }
            }
          }
        )
      case _ => that
  }

  /** @return JSON encoded representation of the document resource */
  def toJson: String = json.noSpacesSortKeys

  /** Validate the JSON value, ensuring that it is a object
    *
    * @return
    *   JSON Document Resource instance on success, else an exception
    */
  private def validate: Try[JsonDocumentResource] =
    json.asObject match
      case Some(_) => Success(this)
      case None    => Failure(new JsonObjectValidationException("JSON must be a JSON object"))

}

object JsonDocumentResource extends StrictLogging {

  /** Alternate constructor, enabling the initialization of a new instance from
    * a string value
    *
    * A `JsonObjectValidationException` will be returned in the case that the
    * value cannot be parsed, of if the parsed value is not a JSON object.
    *
    * @param value
    *   String that represents a valid JSON value
    * @return
    *   JSON Document Resource instance on success, else an exception
    */
  def apply(value: String): Try[JsonDocumentResource] = {
    parse(value) match
      case Right(json) => new JsonDocumentResource(json).validate
      case Left(exception) =>
        logger.warn(exception.getMessage)
        Failure(new JsonObjectValidationException("Unable to parse document resource"))
  }
}
