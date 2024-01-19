package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.model.xapi.common.Equivalence

/** Interaction Component
  *
  * @param id
  *   Identifies the interaction component within the list
  * @param definition
  *   A description of the interaction component (for example, the text for a
  *   given choice in a multiple-choice interaction)
  */
case class InteractionComponent(id: String, definition: Option[LanguageMap] = None)
    extends Equivalence {

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * The signature for an interaction component is computed by concatenating
    * the identifier and the signature of the definition language map with a
    * standard separator and then hashing
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      combine {
        List(id, definition.map(_.signature()).getOrElse(placeholder))
      }
    }
  }
}

object InteractionComponent {
  implicit val decoder: Decoder[InteractionComponent] = deriveDecoder[InteractionComponent]
  implicit val encoder: Encoder[InteractionComponent] =
    deriveEncoder[InteractionComponent].mapJson(_.dropNullValues)
}
