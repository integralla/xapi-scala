package io.integralla.model.xapi.statement

import java.security.MessageDigest
import java.util.Locale

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

  /** Transforms a string to lower case
    * @param str The string to transform
    * @return The string transformed to lower case
    */
  protected def lower(str: String): String = {
    str.toLowerCase(Locale.ROOT)
  }

  /** Combines a list of strings into a single string with a fixed separator
    * @param strings List of strings to combine
    * @return The combined string
    */
  protected def combine(strings: List[String]): String = {
    strings.mkString(separator)
  }

  /** Generates a signature that can be used to test logical equivalence between objects
    * @return A string identifier
    */
  protected[statement] def signature(): String

  /** Compares this instance to another for logical equivalency
    * @param instance The instance to which this will be compared
    * @tparam A The type of the instance to be compared
    * @return True if logically equivalent, else false
    */
  def isEquivalentTo[A <: Equivalence](instance: A): Boolean = {
    this.signature() == instance.signature()
  }

}
