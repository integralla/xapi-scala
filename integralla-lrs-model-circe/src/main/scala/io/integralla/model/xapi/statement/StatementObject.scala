package io.integralla.model.xapi.statement

import io.circe._
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{Account, IRI, MBox}

import java.time.OffsetDateTime
import java.util.UUID

/**
 * This class provides a wrapper around the five statement object types that the `object` property can be set to
 * It is used for encoding / decoding purposes only
 *
 * @param value – An object of one of the five supported statement object types
 */
case class StatementObject(value: AnyRef)

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
              throw new StatementValidationException("An objectType must be explicitly set for any object type other than Activity")
          }
        case Some(StatementObjectType.Activity) =>
          Activity(objectType, c.get[IRI]("id").toOption.get, c.get[ActivityDefinition]("definition").toOption)

        /* Agent Object */
        case Some(StatementObjectType.Agent) => Agent(
          objectType,
          c.get[String]("name").toOption,
          c.get[MBox]("mbox").toOption,
          c.get[String]("mbox_sha1sum").toOption,
          c.get[String]("openid").toOption,
          c.get[Account]("account").toOption
        )

        /* Group Object */
        case Some(StatementObjectType.Group) => Group(
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
          val statementObject: StatementObject = c.downField("object").downField("objectType").as[StatementObjectType].toOption
          match {
            case Some(StatementObjectType.Activity) => StatementObject(c.get[Activity]("object").toOption.get)
            case Some(StatementObjectType.Agent) => StatementObject(c.get[Agent]("object").toOption.get)
            case Some(StatementObjectType.Group) => StatementObject(c.get[Group]("object").toOption.get)
            case Some(StatementObjectType.StatementRef) => StatementObject(c.get[StatementRef]("object").toOption.get)
            case Some(StatementObjectType.SubStatement) => throw new StatementValidationException("A sub-statement cannot contain a sub-statement of it's own")
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

        case _ => throw new StatementValidationException("An object must be one of Activity, Agent, Group, StatementRef, or SubStatement")
      }

    } yield {
      StatementObject(statementObject)
    }
  }

  implicit val encoder: Encoder[StatementObject] = (a: StatementObject) => {
    a.value match {
      case activity@Activity(_, _, _) => activity.asJson
      case agent@Agent(_, _, _, _, _, _) => agent.asJson
      case group@Group(_, _, _, _, _, _, _) => group.asJson
      case statementRef@StatementRef(_, _) => statementRef.asJson
      case subStatement@SubStatement(_, _, _, _, _, _, _, _) => subStatement.asJson
    }
  }
}
