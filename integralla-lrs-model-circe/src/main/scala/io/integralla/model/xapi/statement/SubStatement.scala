package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType
import io.integralla.model.xapi.statement.exceptions.StatementValidationException

import java.time.OffsetDateTime

/**
 * A SubStatement is a type of object that can be used to represent an experience that has not already occurred
 *
 * @param objectType SubStatement
 * @param actor An agent or group that identifies whom the statement is about
 * @param verb The action taken by the actor
 * @param `object` An agent, activity, or another statement that is the object of the statement
 * @param result A result object that provides further details representing a measured outcome
 * @param context A context object that provides additional meaning for the statement
 * @param timestamp The time at which the experience occurred
 * @param attachments An array of attachment objects which provide headers for any attachments associated with the statement
 */
case class SubStatement(
  objectType: StatementObjectType = StatementObjectType.SubStatement,
  actor: StatementActor,
  verb: StatementVerb,
  `object`: StatementObject,
  result: Option[StatementResult],
  context: Option[StatementContext],
  timestamp: Option[OffsetDateTime],
  attachments: Option[List[Attachment]]
) extends StatementModelValidation {

  override def validate(): Unit = {
    validateObjectIsNotSubStatement()
  }

  def validateObjectIsNotSubStatement(): Unit = {
    if (`object`.value.isInstanceOf[SubStatement]) {
      throw new StatementValidationException("A sub-statement cannot contain a sub-statement of it's own")
    }

  }
}

object SubStatement extends StatementModelBase {
  override type T = SubStatement
  override implicit val decoder: Decoder[SubStatement] = deriveDecoder[SubStatement]
  override implicit val encoder: Encoder[SubStatement] = deriveEncoder[SubStatement].mapJson(_.dropNullValues)
}