package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType

import java.time.OffsetDateTime

/**
 * A SubStatement is a type of object that can be used to represent an experience that has not already occurred
 *
 * @param objectType  SubStatement
 * @param actor       An agent or group that identifies whom the statement is about
 * @param verb        The action taken by the actor
 * @param `object`    An agent, activity, or another statement that is the object of the statement
 * @param result      A result object that provides further details representing a measured outcome
 * @param context     A context object that provides additional meaning for the statement
 * @param timestamp   The time at which the experience occurred
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
) extends StatementValidation {

  override def validate: Seq[Either[String, Boolean]] = {
    Seq(validateObjectIsNotSubStatement)
  }

  def validateObjectIsNotSubStatement: Either[String, Boolean] = {
    if (`object`.value.isInstanceOf[SubStatement]) {
      Left("A sub-statement cannot contain a sub-statement of it's own")
    } else {
      Right(true)
    }
  }
}

object SubStatement {
  implicit val decoder: Decoder[SubStatement] = deriveDecoder[SubStatement]
  implicit val encoder: Encoder[SubStatement] = deriveEncoder[SubStatement].mapJson(_.dropNullValues)
}