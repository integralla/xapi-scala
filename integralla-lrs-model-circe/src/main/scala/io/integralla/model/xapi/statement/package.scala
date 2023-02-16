package io.integralla.model.xapi

import io.integralla.model.xapi.statement.identifiers.IRI

import java.security.MessageDigest
import java.util.Locale

package object statement {

  /** A language map is a dictionary where the key is a RFC 5646 Language Tag, and the value is a string in the language specified in the tag. */
  type LanguageMap = Map[String, String]

  /** Extensions are defined by a map and logically relate to the part of the Statement where they are present.
    * The keys of an extension map must be an IRI.
    * The values of an extension map can be any JSON value or data structure.
    */
  type Extensions = Map[IRI, io.circe.Json]

  /** Generates an MD5 hash of a string
    * @param str The string to hash
    * @return A hash string
    */
  def md5(str: String): String = {
    MessageDigest
      .getInstance("MD5")
      .digest(str.toLowerCase(Locale.ENGLISH).getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }

}
