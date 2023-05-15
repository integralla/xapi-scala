package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.identifiers.IRI

/** A Verb defines the action between an Actor and an Activity
  *
  * @param id      An IRI that corresponds to a Verb definition
  * @param display A language map where the key is a RFC 5646 Language Tag, and the value is a string in the language specified in the tag
  */
case class StatementVerb(id: IRI, display: Option[LanguageMap]) extends Equivalence {

  /** Generates a signature that can be used to test logical equivalence between objects
    * For a statement verb, only the verb identifier is used
    *  @return A string identifier
    */
  override protected[statement] def signature(): String = {
    id.signature()
  }
}

object StatementVerb {
  implicit val decoder: Decoder[StatementVerb] = deriveDecoder[StatementVerb]
  implicit val encoder: Encoder[StatementVerb] = deriveEncoder[StatementVerb].mapJson(_.dropNullValues)
}
