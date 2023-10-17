package io.integralla.model.xapi.exceptions

/** This exception is used to indicate that that the model cannot be parsed or
  * decoded
  *
  * @param message
  *   Message providing an explanation of the error
  */
class LRSModelDecodingException(message: String) extends Exception(message)
