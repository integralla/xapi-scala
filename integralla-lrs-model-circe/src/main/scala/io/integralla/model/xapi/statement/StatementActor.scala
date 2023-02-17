package io.integralla.model.xapi.statement

import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import io.integralla.model.utils.Equivalence
import io.integralla.model.xapi.statement.StatementObjectType.StatementObjectType
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.{Account, MBox}
import io.lemonlabs.uri.AbsoluteUrl

import scala.util.{Failure, Success}

/** Actor
  * Used to define whom a statement is about
  * An actor can be either an agent or a group
  */
sealed trait StatementActor extends StatementValidation with Equivalence {

  /** @return The actor name */
  def name: Option[String]

  /** @return The actor mbox value */
  def mbox: Option[MBox]

  /** @return The actor mbox_sha1sum value */
  def mbox_sha1sum: Option[String]

  /** @return The actor openid value */
  def openid: Option[String]

  /** @return The actor account object */
  def account: Option[Account]

  /** @return The IFI type name */
  def ifiType(): Option[String] = {
    val types = List("mbox", "mbox_sha1sum", "openid", "account")
    val options = List(mbox, mbox_sha1sum, openid, account)
    val actual = options.zip(types).filter(_._1.isDefined)
    actual.size match {
      case 0 => None
      case 1 => Some(actual.head._2)
      case _ => throw new RuntimeException("An actor cannot have more than one IFI")
    }

  }

  /** @return The IFI value as a string */
  def ifiValue(): Option[String] = {
    ifiType().map {
      case "mbox"         => mbox.get.value
      case "mbox_sha1sum" => mbox_sha1sum.get
      case "openid"       => openid.get
      case "account"      => List(account.get.homePage, account.get.name).mkString("#")
      case _              => throw new RuntimeException("Unrecognized IFI type")
    }
  }

  /** @return An IFI key composed of it's type and value */
  def ifiKey(): Option[String] = {
    if (ifiType().isDefined & ifiValue().isDefined) {
      Some(List(ifiType().get, ifiValue().get).mkString("#"))
    } else {
      None
    }

  }

  /** @return The actor type (Agent, Group) */
  def actorType(): StatementObjectType

  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      checkOpenId
    )
  }

  override protected def signature(): String = {
    actorType() match {
      case StatementObjectType.Agent => this.asInstanceOf[Agent].signature()
      case StatementObjectType.Group => this.asInstanceOf[Group].signature()
    }
  }

  private def checkOpenId: Either[String, Boolean] = {
    openid
      .map(openid => {
        AbsoluteUrl.parseTry(openid) match {
          case Failure(_) => Left("An Actor openid identifier must be a valid URL")
          case Success(_) => Right(true)
        }
      }).getOrElse(Right(true))
  }
}

object StatementActor {

  implicit val decoder: Decoder[StatementActor] = (c: HCursor) =>
    for {
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
    case group @ Group(_, _, _, _, _, _, _) => group.asJson
    case agent @ Agent(_, _, _, _, _, _)    => agent.asJson
  }

}

/** An agent is a individual persona or system
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
) extends StatementActor with Equivalence {

  override def actorType(): StatementObjectType = StatementObjectType.Agent

  override def validate: Seq[Either[String, Boolean]] = {
    super.validate ++ Seq(
      validateInverseFunctionalIdentifier,
      validateObjectType
    )
  }

  override protected[statement] def signature(): String = {
    hash {
      combine {
        List(
          this.objectType.getOrElse(StatementObjectType.Agent).toString,
          this.name.getOrElse(placeholder),
          lower(this.mbox.getOrElse(placeholder).toString),
          lower(this.mbox_sha1sum.getOrElse(placeholder)),
          this.openid.getOrElse(placeholder),
          this.account.map(a => s"${a.homePage}/${a.name}").getOrElse(placeholder)
        )
      }
    }
  }

  private def validateInverseFunctionalIdentifier: Either[String, Boolean] = {
    val ids = List(mbox, mbox_sha1sum, openid, account).filter(_.isDefined)
    ids.length match {
      case 1 => Right(true)
      case 0 => Left("An Agent must have an inverse functional identifier")
      case _ => Left("An Agent must not include more than one inverse functional identifier")
    }
  }

  private def validateObjectType: Either[String, Boolean] = {
    objectType
      .map(objectType => {
        if (objectType != StatementObjectType.Agent) {
          Left("An Agent must have the object type of 'Agent'")
        } else {
          Right(true)
        }
      }).getOrElse(Right(true))
  }
}

object Agent {
  implicit val decoder: Decoder[Agent] = deriveDecoder[Agent]
  implicit val encoder: Encoder[Agent] = deriveEncoder[Agent].mapJson(_.dropNullValues)
}

/** A group represents a collection of agents
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
  * @param member       A collection of agents
  */
case class Group(
  objectType: StatementObjectType,
  name: Option[String],
  mbox: Option[MBox],
  mbox_sha1sum: Option[String],
  openid: Option[String],
  account: Option[Account],
  member: Option[List[Agent]]
) extends StatementActor with Equivalence {

  override def actorType(): StatementObjectType = StatementObjectType.Group

  /** @return A boolean indicating whether a group is anonymous or not */
  def isAnonymous: Boolean = {
    if (ifiType().isDefined) false else true
  }

  override def validate: Seq[Either[String, Boolean]] = {
    super.validate ++ Seq(
      validateInverseFunctionalIdentifier,
      validateObjectType
    )
  }

  override protected[statement] def signature(): String = {
    hash {
      combine {
        List(
          this.objectType.toString,
          this.name.getOrElse(placeholder),
          lower(this.mbox.getOrElse(placeholder).toString),
          lower(this.mbox_sha1sum.getOrElse(placeholder)),
          this.openid.getOrElse(placeholder),
          this.account.map(a => s"${a.homePage}/${a.name}").getOrElse(placeholder),
          this.member
            .map(agents => {
              agents.map(_.signature()).sorted.mkString(separator)
            }).getOrElse(placeholder)
        )
      }
    }
  }

  private def validateInverseFunctionalIdentifier: Either[String, Boolean] = {
    val ids = List(mbox, mbox_sha1sum, openid, account).filter(id => id.isDefined)
    ids.length match {
      case 1 => Right(true)
      case 0 =>
        if (member.isEmpty) {
          Left(
            "A Group must have an inverse functional identifier (identified group) or identified members (anonymous group)"
          )
        } else {
          Right(true)
        }
      case _ => Left("A Group must not include more than one inverse functional identifier")
    }
  }

  private def validateObjectType: Either[String, Boolean] = {
    if (objectType != StatementObjectType.Group) Left("A Group must have the object type of 'Group'")
    else Right(true)
  }
}

object Group {
  implicit val decoder: Decoder[Group] = deriveDecoder[Group]
  implicit val encoder: Encoder[Group] = deriveEncoder[Group].mapJson(_.dropNullValues)
}
