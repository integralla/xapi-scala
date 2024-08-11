package io.integralla.xapi.model.statement

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.integralla.xapi.model.common.{Equivalence, ExtensionMap}
import io.integralla.xapi.model.references.{AgentReference, InstructorRef, TeamRef}

import java.util.UUID

/** Provides a place to add contextual information to a statement
  *
  * @param registration
  *   The registration that the statement is associated with
  * @param instructor
  *   The instructor that the statement relates to, if not included as the actor of the statement
  * @param team
  *   The team (group) that this statement relates to, if not included as the actor of the statement
  * @param contextActivities
  *   A map of the types of learning activity context that this statement is related to
  * @param contextAgents
  *   [xAPI 2.0] Collection of objects each describing a relation between this statement and an
  *   agent, optionally categorized by a list of "Relevant Type" IRIs
  * @param contextGroups
  *   [xAPI 2.0] Collection of objects each describing a relation between this statement and a group
  *   (identified or anonymous), optionally categorized by a list of "Relevant Type" IRIs
  * @param revision
  *   The revision of the learning activity associated with this statement
  * @param platform
  *   The platform used in the experience of this learning activity
  * @param language
  *   An RFC 5646 language tag representing the language in which the experience being recorded in
  *   this statement (mainly) occurred in, if applicable and known
  * @param statement
  *   A reference to another statement to be considered as context for this statement
  * @param extensions
  *   A map of any other domain-specific context relevant to this statement
  */
case class StatementContext(
  registration: Option[UUID] = None,
  instructor: Option[StatementActor] = None,
  team: Option[Group] = None,
  contextActivities: Option[ContextActivities] = None,
  contextAgents: Option[List[ContextAgent]] = None,
  contextGroups: Option[List[ContextGroup]] = None,
  revision: Option[String] = None,
  platform: Option[String] = None,
  language: Option[String] = None,
  statement: Option[StatementRef] = None,
  extensions: Option[ExtensionMap] = None
) extends Equivalence {

  /** Generates a signature that can be used to test logical equivalence between objects
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      combine {
        List(
          registration.map(_.toString).getOrElse(placeholder),
          instructor.map(_.signature()).getOrElse(placeholder),
          team.map(_.signature()).getOrElse(placeholder),
          contextActivities.map(_.signature()).getOrElse(placeholder),
          contextAgents.map(_.map(_.signature()).sorted.mkString(separator)).getOrElse(placeholder),
          contextGroups.map(_.map(_.signature()).sorted.mkString(separator)).getOrElse(placeholder),
          revision.getOrElse(placeholder),
          platform.getOrElse(placeholder),
          language.getOrElse(placeholder),
          statement.map(_.signature()).getOrElse(placeholder),
          extensions.map(_.signature()).getOrElse(placeholder)
        )
      }
    }
  }

  /** A list of agent references composed of the those identified by the instructor and team
    * properties
    *
    * @param inSubStatement
    *   Whether the reference occurs in a sub-statement
    * @return
    *   A list of agent references
    */
  def agentReferences(inSubStatement: Boolean): List[AgentReference] = {
    List(
      instructor.map(_.asList().map(agent => {
        AgentReference(
          agent = agent._1,
          referenceType = InstructorRef,
          inSubStatement = inSubStatement,
          asGroupMember = agent._2
        )
      })),
      team.map(_.asList().map(agent => {
        AgentReference(
          agent = agent._1,
          referenceType = TeamRef,
          inSubStatement = inSubStatement,
          asGroupMember = agent._2
        )
      })),
      contextAgents.map(_.map(_.agentReference(inSubStatement))),
      contextGroups.map(_.flatMap(_.agentReferences(inSubStatement)))
    ).flatMap(_.getOrElse(List.empty[AgentReference])).distinct
  }
}

object StatementContext {
  implicit val decoder: Decoder[StatementContext] = deriveDecoder[StatementContext]
  implicit val encoder: Encoder[StatementContext] =
    deriveEncoder[StatementContext].mapJson(_.dropNullValues)
}
