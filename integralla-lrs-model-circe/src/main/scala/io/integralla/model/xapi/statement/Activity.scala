package io.integralla.model.xapi.statement

import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType
import io.integralla.model.xapi.statement.identifiers.IRI

/** A statement activity
  *
  * @param objectType Activity statement object type
  * @param id         An identifier for a single unique activity
  * @param definition An activity definition
  */
case class Activity(objectType: Option[StatementObjectType], id: IRI, definition: Option[ActivityDefinition])
    extends Equivalence {

  /** Generates a signature for what the object logically represents
    *
    * The signature of an activity is based solely on it's identifier
    * which is handled as a IRI. As per the specification, the activity
    * definition is not considered as part of the immutable statement
    * definition
    *
    * @return A string identifier
    */
  override protected[statement] def signature(): String = {
    id.signature()
  }
}

object Activity {
  implicit val decoder: Decoder[Activity] = deriveDecoder[Activity]
  implicit val encoder: Encoder[Activity] = deriveEncoder[Activity].mapJson(_.dropNullValues)
}
