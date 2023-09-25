package io.integralla.model.exceptions

/** This exception is used to indicate that that a statement does not validate
  * against the xAPI specification
  *
  * @param message
  *   Message providing an explanation of the error
  */
class StatementValidationException(message: String) extends Exception(message)
