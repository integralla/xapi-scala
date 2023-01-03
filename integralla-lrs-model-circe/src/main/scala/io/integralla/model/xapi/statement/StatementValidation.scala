package io.integralla.model.xapi.statement

import io.integralla.model.xapi.statement.exceptions.StatementValidationException

trait StatementValidation {

  def validate: Seq[Either[String, Boolean]]

  val exceptions: Seq[String] = validate
    .map(_.swap)
    .flatMap(_.toSeq)

  if (exceptions.nonEmpty) {
    throw new StatementValidationException(s"Statement Validation Errors: ${exceptions.mkString("; ")}")
  }

}
