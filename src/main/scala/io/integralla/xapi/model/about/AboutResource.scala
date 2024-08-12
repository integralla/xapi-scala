package io.integralla.xapi.model.about

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.xapi.model.common.ExtensionMap
import io.integralla.xapi.model.utils.LRSModel

/** Model for the about resource
  *
  * @param version
  *   A list of versions, composed of the supported latest versions for each
  *   major version
  * @param extensions
  *   A map of other properties, adhering to the format of an extension map
  */
case class AboutResource(version: List[String], extensions: Option[ExtensionMap]) extends LRSModel

object AboutResource {
  implicit val decoder: Decoder[AboutResource] = deriveDecoder[AboutResource]
  implicit val encoder: Encoder[AboutResource] =
    deriveEncoder[AboutResource].mapJson(_.dropNullValues)
}
