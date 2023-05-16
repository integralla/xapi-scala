package io.integralla.model.xapi.about

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.model.xapi.common.ExtensionMap

/** Model for the about resource
  *
  * @param versions A list of versions, composed of the supported latest versions for each major version
  * @param extensions A map of other properties, adhering to the format of an extension map
  */
case class AboutResource(versions: List[String], extensions: Option[ExtensionMap])

object AboutResource {
  implicit val decoder: Decoder[AboutResource] = deriveDecoder[AboutResource]
  implicit val encoder: Encoder[AboutResource] = deriveEncoder[AboutResource].mapJson(_.dropNullValues)
}
