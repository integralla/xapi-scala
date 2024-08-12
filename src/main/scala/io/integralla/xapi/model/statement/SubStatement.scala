package io.integralla.xapi.model.statement

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import StatementObjectType.StatementObjectType
import io.integralla.xapi.model.common.Equivalence
import io.integralla.xapi.model.references.{ActivityReference, ActorRef, AgentReference}

import java.time.OffsetDateTime

/** A SubStatement is a type of object that can be used to represent an
  * experience that has not already occurred
  *
  * @param objectType
  *   SubStatement
  * @param actor
  *   An agent or group that identifies whom the statement is about
  * @param verb
  *   The action taken by the actor
  * @param `object`
  *   An agent, activity, or another statement that is the object of the
  *   statement
  * @param result
  *   A result object that provides further details representing a measured
  *   outcome
  * @param context
  *   A context object that provides additional meaning for the statement
  * @param timestamp
  *   The time at which the experience occurred
  * @param attachments
  *   An array of attachment objects which provide headers for any attachments
  *   associated with the statement
  */
case class SubStatement(
  objectType: StatementObjectType = StatementObjectType.SubStatement,
  actor: StatementActor,
  verb: StatementVerb,
  `object`: StatementObject,
  result: Option[StatementResult] = None,
  context: Option[StatementContext] = None,
  timestamp: Option[OffsetDateTime] = None,
  attachments: Option[List[Attachment]] = None
) extends StatementValidation with Equivalence {

  /** @return
    *   A distinct list of all activities referenced in the sub-statement
    */
  def activityReferences: List[ActivityReference] = {
    List(
      `object`.activityReferences(true),
      context
        .flatMap(_.contextActivities.map(_.activityReferences(true)))
        .getOrElse(List.empty[ActivityReference])
    ).flatten.distinct
  }

  /** A list of agent references composed of the those identified within a
    * sub-statement
    *
    * @return
    *   A list of agent references
    */
  def agentReferences: List[AgentReference] = {
    List(
      actor
        .asList().map(agent => {
          AgentReference(
            agent = agent._1,
            referenceType = ActorRef,
            inSubStatement = true,
            asGroupMember = agent._2
          )
        }),
      `object`.agentReferences(inSubStatement = true),
      context
        .map(context => context.agentReferences(inSubStatement = true)).getOrElse(
          List.empty[AgentReference]
        )
    ).flatten.distinct
  }

  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateObjectIsNotSubStatement,
      validateContextPropertiesRevision,
      validateContextPropertiesPlatform
    )
  }

  private def validateObjectIsNotSubStatement: Either[String, Boolean] = {
    if (`object`.value.isInstanceOf[SubStatement]) {
      Left("A sub-statement cannot contain a sub-statement of it's own")
    } else {
      Right(true)
    }
  }

  private def validateContextPropertiesRevision: Either[String, Boolean] = {
    context
      .map((statementContext: StatementContext) => {
        `object`.value match {
          case Activity(_, _, _) => Right(true)
          case _ =>
            if (statementContext.revision.isDefined) {
              Left(
                """The "revision" property on the context object must only be used if the statement's object is an activity"""
              )
            } else {
              Right(true)
            }
        }
      }).getOrElse(Right(true))
  }

  private def validateContextPropertiesPlatform: Either[String, Boolean] = {
    context
      .map((statementContext: StatementContext) => {
        `object`.value match {
          case Activity(_, _, _) => Right(true)
          case _ =>
            if (statementContext.platform.isDefined) {
              Left(
                """The "platform" property on the context object must only be used if the statement's object is an activity"""
              )
            } else {
              Right(true)
            }
        }
      }).getOrElse(Right(true))
  }

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      combine {
        List(
          objectType.toString,
          actor.signature(),
          verb.signature(),
          `object`.signature(),
          result.map(_.signature()).getOrElse(placeholder),
          context.map(_.signature()).getOrElse(placeholder)
        )
      }
    }
  }
}

object SubStatement {
  implicit val decoder: Decoder[SubStatement] = deriveDecoder[SubStatement]
  implicit val encoder: Encoder[SubStatement] =
    deriveEncoder[SubStatement].mapJson(_.dropNullValues)
}
