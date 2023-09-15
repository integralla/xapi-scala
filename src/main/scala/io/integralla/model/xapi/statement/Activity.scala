package io.integralla.model.xapi.statement

import io.circe.*
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.model.xapi.common.Equivalence
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType
import io.integralla.model.xapi.statement.identifiers.IRI

/** A statement activity
  *
  * @param objectType
  *   Activity statement object type
  * @param id
  *   An identifier for a single unique activity
  * @param definition
  *   An activity definition
  */
case class Activity(
  objectType: Option[StatementObjectType] = None,
  id: IRI,
  definition: Option[ActivityDefinition] = None
) extends Equivalence {

  /** Similar to `isEquivalentTo` but includes the activity definition in
    * addition to the activity identifier
    *
    * @param instance
    *   The instance to test logical equivalence against
    * @return
    *   True if both instances are logically equivalent, else false
    */
  def isEquivalentToFull(instance: Activity): Boolean = {
    List(
      this.signature() == instance.signature(),
      if (List(this.definition, instance.definition).forall(_.isDefined)) {
        this.definition.get.isEquivalentTo(instance.definition.get)
      } else if (List(this.definition, instance.definition).forall(_.isEmpty)) {
        true
      } else false
    ).forall(_ == true)
  }

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * The signature of an activity is based solely on it's identifier which is
    * handled as a IRI. As per the specification, the activity definition is not
    * considered as part of the immutable statement definition
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    id.signature()
  }
}

object Activity {
  implicit val decoder: Decoder[Activity] = deriveDecoder[Activity]
  implicit val encoder: Encoder[Activity] = deriveEncoder[Activity].mapJson(_.dropNullValues)
}
