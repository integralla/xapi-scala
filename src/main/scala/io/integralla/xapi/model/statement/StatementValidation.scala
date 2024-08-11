package io.integralla.xapi.model.statement

import io.integralla.xapi.model.exceptions.StatementValidationException

/** Trait to be extended by models that provide property validation
  *
  * On initialization of a class that extends this trait, validation will be performed automatically
  * and an `StatementValidationException` will be thrown if any validation failures are detected.
  */
trait StatementValidation {

  /** Abstract method to be implemented by extending classes which performs one or more validations
    * and returns the results as a sequence which will be further processed by this trait
    *
    * @return
    *   Sequence whose values are an `Either` where `Left` provides a description of validation
    *   exception and `Right` represents a boolean indicating that validation succeeded
    */
  def validate: Seq[Either[String, Boolean]]

  /** List of exception messages returned by any failures produced by `validate` */
  val exceptions: Seq[String] = validate
    .map(_.swap)
    .flatMap(_.toSeq)

  /** As part of the trait's constructor, this code will be run on initialization of any extending
    * class, throwing an exception if any validation failures are detected
    */
  if (exceptions.nonEmpty) {
    throw new StatementValidationException(
      s"Statement Validation Errors: ${exceptions.mkString("; ")}"
    )
  }

}
