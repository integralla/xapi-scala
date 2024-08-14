package io.integralla.xapi.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import StatementVerb.voidingVerb
import io.integralla.xapi.model.common.{Decodable, Encodable, Equivalence}

/** A Verb defines the action between an Actor and an Activity
  *
  * @param id
  *   An IRI that corresponds to a Verb definition
  * @param display
  *   A language map where the key is a RFC 5646 Language Tag, and the value is
  *   a string in the language specified in the tag
  */
case class StatementVerb(id: IRI, display: Option[LanguageMap] = None)
    extends Encodable[StatementVerb] with Equivalence {

  /** Indicates whether the verb is a voiding verb
    *
    * @return
    *   True if the verb is http://adlnet.gov/expapi/verbs/voided, else false
    */
  def isVoiding: Boolean = {
    if (id == voidingVerb) true else false
  }

  /** Generates a signature that can be used to test logical equivalence between
    * objects For a statement verb, only the verb identifier is used
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    id.signature()
  }
}

object StatementVerb extends Decodable[StatementVerb] {

  /** The IRI value expected for a voiding statement */
  val voidingVerb: IRI = IRI("http://adlnet.gov/expapi/verbs/voided")

  implicit val decoder: Decoder[StatementVerb] = deriveDecoder[StatementVerb]
  implicit val encoder: Encoder[StatementVerb] =
    deriveEncoder[StatementVerb].mapJson(_.dropNullValues)
}
