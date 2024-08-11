package io.integralla.xapi.model.exceptions

/** This exception is used to indicate that an error has occurred parsing a JSON object, including
  * the scenarios where the JSON is not valid or is not a JSON object
  *
  * @param message
  *   Message providing an explanation of the error
  */
class JsonObjectValidationException(message: String) extends Exception(message)
