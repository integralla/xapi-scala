package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.OffsetDateTime
import java.util.UUID

/** Statements are the evidence for any sort of experience or event which is to be tracked in xAPI
  *
  * @param id          A UUID used to uniquely identify the statement
  * @param actor       An agent or group that identifies whom the statement is about
  * @param verb        The action taken by the actor
  * @param `object`    An agent, activity, or another statement that is the object of the statement
  * @param result      A result object that provides further details representing a measured outcome
  * @param context     A context object that provides additional meaning for the statement
  * @param timestamp   The time at which the experience occurred
  * @param stored      The time at which this statement was persisted by the LRS
  * @param authority   An agent or group object indicating who is asserting the validity of the statement
  * @param version     The statement’s associated xAPI version, formatted according to Semantic Versioning 1.0.0
  * @param attachments An array of attachment objects which provide headers for any attachments associated with the statement
  */
case class Statement(
  id: Option[UUID],
  actor: StatementActor,
  verb: StatementVerb,
  `object`: StatementObject,
  result: Option[StatementResult],
  context: Option[StatementContext],
  timestamp: Option[OffsetDateTime],
  stored: Option[OffsetDateTime],
  authority: Option[StatementActor],
  version: Option[String],
  attachments: Option[List[Attachment]]
) extends StatementValidation with Equivalence {

  /** A list of actors referenced by the statement
    * @return A list of identified actors
    */
  def getActorReferences: List[StatementActor] = {
    List(
      actor.asList(),
      `object`.getActorReferences,
      context.map(context => context.getActorReferences).getOrElse(List.empty[StatementActor]),
      authority.map(authority => authority.asList()).getOrElse(List.empty[StatementActor])
    ).flatten.distinct
  }

  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateContextPropertiesRevision,
      validateContextPropertiesPlatform
    )
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

  /** Generates a signature that can be used to test logical equivalence between objects
    *
    * @return A string identifier
    */
  override protected[statement] def signature(): String = {
    hash {
      combine {
        List(
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

object Statement {
  implicit val decoder: Decoder[Statement] = deriveDecoder[Statement]
  implicit val encoder: Encoder[Statement] = deriveEncoder[Statement].mapJson(_.dropNullValues)
}
