package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

/**
 * Interaction Component
 *
 * @param id         Identifies the interaction component within the list
 * @param definition A description of the interaction component (for example, the text for a given choice in a multiple-choice interaction)
 */
case class InteractionComponent(id: String, definition: Option[LanguageMap])

object InteractionComponent extends StatementModelBase {
  override type T = InteractionComponent
  override implicit val decoder: Decoder[InteractionComponent] = deriveDecoder[InteractionComponent]
  override implicit val encoder: Encoder[InteractionComponent] = deriveEncoder[InteractionComponent].mapJson(_.dropNullValues)
}
