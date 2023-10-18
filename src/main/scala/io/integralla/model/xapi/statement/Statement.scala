package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.model.utils.LRSModelUtils
import io.integralla.model.xapi.common.{Equivalence, XApiVersion}
import io.integralla.model.xapi.common.CustomEncoders.*
import io.integralla.model.xapi.references.{ActivityReference, ActorRef, AgentReference, AuthorityRef}

import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.UUID

/** Statements are the evidence for any sort of experience or event which is to
  * be tracked in xAPI
  *
  * @param id
  *   A UUID used to uniquely identify the statement
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
  * @param stored
  *   The time at which this statement was persisted by the LRS
  * @param authority
  *   An agent or group object indicating who is asserting the validity of the
  *   statement
  * @param version
  *   The statement’s associated xAPI version, formatted according to Semantic
  *   Versioning 1.0.0
  * @param attachments
  *   An array of attachment objects which provide headers for any attachments
  *   associated with the statement
  */
case class Statement(
  id: Option[UUID] = None,
  actor: StatementActor,
  verb: StatementVerb,
  `object`: StatementObject,
  result: Option[StatementResult] = None,
  context: Option[StatementContext] = None,
  timestamp: Option[OffsetDateTime] = None,
  stored: Option[OffsetDateTime] = None,
  authority: Option[StatementActor] = None,
  version: Option[XApiVersion] = None,
  attachments: Option[List[Attachment]] = None
) extends StatementValidation with Equivalence {

  /** Extracts and returns all activities (if any) referenced by the statement
    * @return
    *   A distinct list of all activities referenced in the statement
    */
  def activityReferences: List[ActivityReference] = {
    List(
      `object`.activityReferences(),
      context.flatMap(_.contextActivities.map(_.activityReferences())).getOrElse(List.empty[ActivityReference])
    ).flatten.distinct
  }

  /** A list of agent references across all parts of the statement
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
            inSubStatement = false,
            asGroupMember = agent._2
          )
        }),
      `object`.agentReferences(inSubStatement = false),
      context.map(context => context.agentReferences(inSubStatement = false)).getOrElse(List.empty[AgentReference]),
      authority
        .map(_.asList().map(agent => {
          AgentReference(
            agent = agent._1,
            referenceType = AuthorityRef,
            inSubStatement = false,
            asGroupMember = agent._2
          )
        }))
        .getOrElse(List.empty[AgentReference])
    ).flatten.distinct
  }

  /** Extracts and returns all attachment objects from a statement and/or its
    * sub-statement
    *
    * @return
    *   List of attachment objects
    */
  def getAttachments: List[Attachment] =
    List(
      attachments,
      `object`.value match
        case subStatement: SubStatement => subStatement.attachments
        case _                          => None
    ).flatMap(_.getOrElse(List.empty[Attachment]))

  /** Tests whether the statement is a voiding statement
    *
    * @return
    *   True if the statement is a voiding statement, else false
    */
  def isVoidingStatement: Boolean = {
    `object`.value match {
      case _: StatementRef =>
        if (verb.id.value == "http://adlnet.gov/expapi/verbs/voided") true else false
      case _ => false
    }
  }

  /** @return
    *   The size of the statement in bytes based on the JSON representation of
    *   the statement, when printed with no spaces
    */
  def size: Int =
    LRSModelUtils
      .toJSON[Statement](this)
      .getBytes(StandardCharsets.UTF_8).length

  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateAuthority,
      validateVoidingStatement,
      validateContextPropertiesRevision,
      validateContextPropertiesPlatform
    )
  }

  private def validateAuthority: Either[String, Boolean] = {
    authority.fold(Right(true)) {
      case _: Agent => Right(true)
      case group: Group =>
        if (group.isAnonymous) {
          if (group.member.fold(0)(_.length) == 2) {
            if (group.member.get.exists(_.account.isDefined)) {
              Right(true)
            } else {
              Left("An OAuth consumer represented by an authority group member must be identified by account")
            }
          } else {
            Left("An authority represented as a group must have exactly two members")
          }
        } else {
          Left("An authority cannot be an identified group")
        }
    }
  }

  private def validateVoidingStatement: Either[String, Boolean] = {
    if (verb.id.value == "http://adlnet.gov/expapi/verbs/voided") {
      `object`.value match
        case _: StatementRef => Right(true)
        case _ =>
          Left(
            """The reserved verb http://adlnet.gov/expapi/verbs/voided can only be used when the statement object is a statement reference"""
          )
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
