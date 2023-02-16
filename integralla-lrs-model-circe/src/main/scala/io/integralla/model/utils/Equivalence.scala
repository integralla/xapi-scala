package io.integralla.model.utils

import java.security.MessageDigest

/** Provides methods for comparing one instance of an object that implements this trait to another */
trait Equivalence {

  /** Default string placeholder for undefined values */
  protected val placeholder: String = "^^^"

  /** Default string separator for making a string from a list of values */
  protected val separator: String = "#"

  /** Generates an MD5 hash of a string
    * @param str The string to hash
    * @return A hexadecimal string representation of the hash
    */
  protected def hash(str: String): String = {
    MessageDigest
      .getInstance("MD5")
      .digest(str.getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }

  /** Generates a signature for what the object logically represents
    * @return A string identifier
    */
  protected def signature(): String

  /** Compares this instance to another for logical equivalency
    * @param instance The instance to which this will be compared
    * @tparam A The type of the instance to be compared
    * @return True if logically equivalent, else false
    */
  def compare[A <: Equivalence](instance: A): Boolean = {
    this.signature() == instance.signature()
  }

}
