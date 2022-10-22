package io.integralla.model.xapi.statement

import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{Account, MBox}
import io.lemonlabs.uri.AbsoluteUrl

/**
 * Actor
 * Used to define whom a statement is about
 * An actor can be either an agent or a group
 */
sealed trait StatementActor extends StatementModelValidation {

  def name: Option[String]

  def mbox: Option[MBox]

  def mbox_sha1sum: Option[String]

  def openid: Option[String]

  def account: Option[Account]

  override def validate(): Unit = {
    checkOpenId()
  }

  private def checkOpenId(): Unit = {
    if (openid.isDefined) {
      try {
        val _ = AbsoluteUrl.parse(openid.get)
      } catch {
        case _: Throwable => throw new StatementValidationException("An Actor openid identifier must be a valid URL")
      }
    }
  }

}

object StatementActor {

  implicit val decoder: Decoder[StatementActor] = (c: HCursor) => for {
    objectType <- c.get[Option[StatementObjectType]]("objectType")
    name <- c.get[Option[String]]("name")
    mbox <- c.get[Option[MBox]]("mbox")
    mbox_sha1sum <- c.get[Option[String]]("mbox_sha1sum")
    openid <- c.get[Option[String]]("openid")
    account <- c.get[Option[Account]]("account")
    member <- c.get[Option[List[Agent]]]("member")

    actor: StatementActor = objectType match {
      case None | Some(StatementObjectType.Agent) => Agent(objectType, name, mbox, mbox_sha1sum, openid, account)
      case Some(StatementObjectType.Group) => Group(objectType.get, name, mbox, mbox_sha1sum, openid, account, member)
      case _ => throw new StatementValidationException(s"$objectType is not a supported objectType for an actor")
    }
  } yield actor

  implicit val encoder: Encoder[StatementActor] = Encoder.instance[StatementActor] {
    case group@Group(_, _, _, _, _, _, _) => group.asJson
    case agent@Agent(_, _, _, _, _, _) => agent.asJson
  }

}

/**
 * An agent is a individual persona or system
 *
 * @param objectType   The Agent object type
 * @param name         The agent name
 * @param mbox         A mailbox identifier
 * @param mbox_sha1sum A SHA1 checksum of the agent's mailbox identifier
 * @param openid       An openId identifier
 * @param account      An account identifier object
 */
case class Agent(
  objectType: Option[StatementObjectType],
  name: Option[String],
  mbox: Option[MBox],
  mbox_sha1sum: Option[String],
  openid: Option[String],
  account: Option[Account]
) extends StatementActor {

  override def validate(): Unit = {
    super.validate()
    validateInverseFunctionalIdentifier()
    validateObjectType()
  }

  private def validateInverseFunctionalIdentifier(): Unit = {
    val ids = List(mbox, mbox_sha1sum, openid, account).filter(id => id.isDefined)
    ids.length match {
      case 1 => ()
      case 0 => throw new StatementValidationException("An Agent must have an inverse functional identifier")
      case _ => throw new StatementValidationException("An Agent must not include more than one inverse functional identifier")
    }
  }

  private def validateObjectType(): Unit = {
    if (objectType.isDefined) {
      if (objectType.get != StatementObjectType.Agent) throw new StatementValidationException("An Agent must have the object type of 'Agent'")
    }
  }

}

object Agent {
  implicit val decoder: Decoder[Agent] = deriveDecoder[Agent]
  implicit val encoder: Encoder[Agent] = deriveEncoder[Agent].mapJson(_.dropNullValues)
}

/**
 * A group represents a collection of agents
 * A group can be anonymous (no inverse functional identifier is provided)
 * Or, a group can be identified (a single inverse identifier is provided)
 *
 * An anonymous group must have members
 *
 * @param objectType   The Group object type
 * @param name         The group name
 * @param mbox         A mailbox identifier
 * @param mbox_sha1sum A SHA1 checksum of the group's mailbox identifier
 * @param openid       An openId identifier
 * @param account      An account identifier object
 * @param member      A collection of agents
 */
case class Group(
  objectType: StatementObjectType,
  name: Option[String],
  mbox: Option[MBox],
  mbox_sha1sum: Option[String],
  openid: Option[String],
  account: Option[Account],
  member: Option[List[Agent]]
) extends StatementActor {

  override def validate(): Unit = {
    super.validate()
    validateInverseFunctionalIdentifier()
    validateObjectType()
  }

  private def validateInverseFunctionalIdentifier(): Unit = {
    val ids = List(mbox, mbox_sha1sum, openid, account).filter(id => id.isDefined)
    ids.length match {
      case 1 => ()
      case 0 =>
        if (member.isEmpty) {
          throw new StatementValidationException("A Group must have an inverse functional identifier (identified group) or identified members (anonymous group)")
        }
      case _ => throw new StatementValidationException("A Group must not include more than one inverse functional identifier")
    }
  }

  private def validateObjectType(): Unit = {
    if (objectType != StatementObjectType.Group) throw new StatementValidationException("A Group must have the object type of 'Group'")
  }
}

object Group {
  implicit val decoder: Decoder[Group] = deriveDecoder[Group]
  implicit val encoder: Encoder[Group] = deriveEncoder[Group].mapJson(_.dropNullValues)
}