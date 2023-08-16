package io.integralla.model.xapi.statement

import io.circe.*
import io.circe.syntax.EncoderOps
import io.integralla.model.references.{ActivityObjectRef, ActivityReference, AgentObjectRef, AgentReference}
import io.integralla.model.xapi.common.Equivalence
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{Account, IRI, MBox}

import java.time.OffsetDateTime
import java.util.UUID

/** This class provides a wrapper around the five statement object types that the `object` property can be set to
  * It is used for encoding / decoding purposes only
  *
  * @param value – An object of one of the five supported statement object types
  */
case class StatementObject(value: AnyRef) extends Equivalence {

  /** A list of activities (if any) referenced by the statement object
    * Activities can be referenced via the statement object, or in a sub-statement
    * @return A list of activities
    */
  def getActivityReferences(inSubStatement: Boolean = false): List[ActivityReference] = {
    value match {
      case activity: Activity         => List(ActivityReference(activity, ActivityObjectRef, inSubStatement))
      case subStatement: SubStatement => subStatement.getActivityReferences
      case _                          => List.empty[ActivityReference]
    }
  }

  /** A list of agent references  composed of the those identified by the statement object
    *
    * @param inSubStatement Whether the reference occurs in a sub-statement
    * @return A list of agent references
    */
  def getAgentReferences(inSubStatement: Boolean): List[AgentReference] = {
    value match {
      case agent: Agent =>
        agent
          .asList().map(agent => {
            AgentReference(
              agent = agent._1,
              referenceType = AgentObjectRef,
              inSubStatement = inSubStatement,
              asGroupMember = agent._2
            )
          })
      case group: Group =>
        group
          .asList().map(agent => {
            AgentReference(
              agent = agent._1,
              referenceType = AgentObjectRef,
              inSubStatement = inSubStatement,
              asGroupMember = agent._2
            )
          })
      case subStatement: SubStatement => subStatement.getAgentReferences
      case _                          => List.empty[AgentReference]
    }
  }

  /** Generates a signature that can be used to test logical equivalence between objects
    * @return A string identifier
    */
  override protected[xapi] def signature(): String = {
    value match {
      case activity: Activity         => activity.signature()
      case agent: Agent               => agent.signature()
      case group: Group               => group.signature()
      case statementRef: StatementRef => statementRef.signature()
      case subStatement: SubStatement => subStatement.signature()
    }
  }
}

object StatementObject {

  implicit val decoder: Decoder[StatementObject] = (c: HCursor) => {
    for {
      objectType <- c.get[Option[StatementObjectType]]("objectType")

      statementObject = objectType match {

        /* Activity Object */
        case None =>
          try {
            Activity(None, c.get[IRI]("id").toOption.get, c.get[ActivityDefinition]("definition").toOption)
          } catch {
            case _: Throwable =>
              throw new StatementValidationException(
                "An objectType must be explicitly set for any object type other than Activity"
              )
          }
        case Some(StatementObjectType.Activity) =>
          Activity(objectType, c.get[IRI]("id").toOption.get, c.get[ActivityDefinition]("definition").toOption)

        /* Agent Object */
        case Some(StatementObjectType.Agent) =>
          Agent(
            objectType,
            c.get[String]("name").toOption,
            c.get[MBox]("mbox").toOption,
            c.get[String]("mbox_sha1sum").toOption,
            c.get[String]("openid").toOption,
            c.get[Account]("account").toOption
          )

        /* Group Object */
        case Some(StatementObjectType.Group) =>
          Group(
            objectType.get,
            c.get[String]("name").toOption,
            c.get[MBox]("mbox").toOption,
            c.get[String]("mbox_sha1sum").toOption,
            c.get[String]("openid").toOption,
            c.get[Account]("account").toOption,
            c.get[List[Agent]]("member").toOption
          )

        /* Statement Reference Object */
        case Some(StatementObjectType.StatementRef) => StatementRef(objectType.get, c.get[UUID]("id").toOption.get)

        /* Sub-Statement Object */
        case Some(StatementObjectType.SubStatement) =>
          val statementObject: StatementObject =
            c.downField("object").downField("objectType").as[StatementObjectType].toOption match {
              case None | Some(StatementObjectType.Activity) => StatementObject(c.get[Activity]("object").toOption.get)
              case Some(StatementObjectType.Agent)           => StatementObject(c.get[Agent]("object").toOption.get)
              case Some(StatementObjectType.Group)           => StatementObject(c.get[Group]("object").toOption.get)
              case Some(StatementObjectType.StatementRef) => StatementObject(c.get[StatementRef]("object").toOption.get)
              case Some(StatementObjectType.SubStatement) =>
                throw new StatementValidationException("A sub-statement cannot contain a sub-statement of it's own")
              case _ => throw new StatementValidationException("Unsupported object type")
            }

          SubStatement(
            objectType.get,
            c.get[StatementActor]("actor").toOption.get,
            c.get[StatementVerb]("verb").toOption.get,
            statementObject,
            c.get[StatementResult]("result").toOption,
            c.get[StatementContext]("context").toOption,
            c.get[OffsetDateTime]("timestamp").toOption,
            c.get[List[Attachment]]("attachments").toOption
          )

        case _ =>
          throw new StatementValidationException(
            "An object must be one of Activity, Agent, Group, StatementRef, or SubStatement"
          )
      }

    } yield {
      StatementObject(statementObject)
    }
  }

  implicit val encoder: Encoder[StatementObject] = (a: StatementObject) => {
    a.value match {
      case activity @ Activity(_, _, _)                        => activity.asJson
      case agent @ Agent(_, _, _, _, _, _)                     => agent.asJson
      case group @ Group(_, _, _, _, _, _, _)                  => group.asJson
      case statementRef @ StatementRef(_, _)                   => statementRef.asJson
      case subStatement @ SubStatement(_, _, _, _, _, _, _, _) => subStatement.asJson
    }
  }
}
