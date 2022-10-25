package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.InteractionType.{CHOICE, FILL_IN, InteractionType, LIKERT, LONG_FILL_IN, MATCHING, NUMERIC, OTHER, PERFORMANCE, SEQUENCING, TRUE_FALSE}
import io.integralla.model.xapi.statement.identifiers.IRI
import io.lemonlabs.uri.AbsoluteUrl

import scala.util.{Failure, Success}

/**
 * The definition of an activity object
 *
 * @param name                    A Language Map that provides the human readable/visual name of the activity
 * @param description             A Language Map that provides a description of the activity
 * @param `type`                  An IRI that uniquely identifies the type of activity
 * @param moreInfo                An IRI that resolves to a document with human-readable information about the Activity, which could include a way to launch the activity
 * @param interactionType         The type of interaction for a traditional e-learning activity (for example, choice, true-false, matching)
 * @param correctResponsesPattern A pattern representing the correct response to the interaction
 * @param choices                 An interaction component that defines a list of the options available in the interaction for selection or ordering
 * @param scale                   An interaction component that defines a list of the options on the likert scale
 * @param source                  An interaction component that defines a list of sources to be matched against targets
 * @param steps                   An interaction component that defines a list of the elements making up the performance interaction
 * @param target                  An interaction component that defines a list of targets against which sources can be matched
 * @param extensions              A map of other properties as needed
 */
case class ActivityDefinition(
  name: Option[LanguageMap],
  description: Option[LanguageMap],
  `type`: Option[IRI],
  moreInfo: Option[IRI],
  interactionType: Option[InteractionType],
  correctResponsesPattern: Option[List[String]],
  choices: Option[List[InteractionComponent]],
  scale: Option[List[InteractionComponent]],
  source: Option[List[InteractionComponent]],
  steps: Option[List[InteractionComponent]],
  target: Option[List[InteractionComponent]],
  extensions: Option[Extensions]
) extends StatementValidation {
  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateInteractionTypeByType,
      validateInteractionTypeByCorrectResponsePattern,
      validateInteractionTypeByInteractionComponents,
      validateInteractionComponentByInteractionType,
      validateMoreInfoIRL
    )
  }

  def validateInteractionTypeByType: Either[String, Boolean] = {
    val interactionActivityType: String = "http://adlnet.gov/expapi/activities/cmi.interaction"
    `type`.filter(_.value == interactionActivityType)
      .map(_ => {
        interactionType match {
          case Some(_) => Right(true)
          case None => Left("An interaction type must be specified when the activity type is cmi.interaction")
        }
      }).getOrElse(Right(true))
  }

  def validateInteractionTypeByCorrectResponsePattern: Either[String, Boolean] = {
    correctResponsesPattern.map(_ => {
      interactionType match {
        case Some(_) => Right(true)
        case None => Left("An interaction type must be specified when a correct response pattern is defined")
      }
    }).getOrElse(Right(true))
  }

  def validateInteractionTypeByInteractionComponents: Either[String, Boolean] = {
    if (List(choices, scale, source, steps, target).exists(_.isDefined)) {
      interactionType match {
        case Some(_) => Right(true)
        case None => Left("An interaction type must be specified when interaction components are defined")
      }
    }
    else Right(true)
  }

  def validateInteractionComponentByInteractionType: Either[String, Boolean] = {

    interactionType match {
      case Some(value) =>
        value match {
          case CHOICE | SEQUENCING =>
            if (List(scale, source, target, steps).exists(_.isDefined)) {
              Left(s"The $value interaction type only supports the choices interaction component")
            } else {
              Right(true)
            }
          case FILL_IN | LONG_FILL_IN | NUMERIC | OTHER | TRUE_FALSE =>
            if (List(choices, scale, source, target, steps).exists(_.isDefined)) {
              Left(s"The $value interaction type does not support any interaction components")
            } else {
              Right(true)
            }
          case LIKERT =>
            if (List(choices, source, target, steps).exists(_.isDefined)) {
              Left(s"The $value interaction type only supports the scale interaction component")
            } else {
              Right(true)
            }
          case MATCHING =>
            if (List(choices, scale, steps).exists(_.isDefined)) {
              Left(s"The $value interaction type only supports the source and target interaction components")
            } else {
              Right(true)
            }
          case PERFORMANCE =>
            if (List(choices, scale, source, target).exists(_.isDefined)) {
              Left(s"The $value interaction type only supports the steps interaction component")
            } else {
              Right(true)
            }
        }
      case None =>
        if (List(choices, scale, source, target, steps).exists(_.isDefined)) {
          Left("Interaction Activities MUST have a valid interactionType")
        } else Right(true)
    }
  }

  def validateMoreInfoIRL: Either[String, Boolean] = {
    moreInfo.map((iri: IRI) => {
      AbsoluteUrl.parseTry(iri.value) match {
        case Failure(exception) => Left(f"The value of moreInfo must be a valid IRL: ${exception.getMessage}")
        case Success(_) => Right(true)
      }
    }).getOrElse(Right(true))
  }
}

object ActivityDefinition {
  implicit val decoder: Decoder[ActivityDefinition] = deriveDecoder[ActivityDefinition]
  implicit val encoder: Encoder[ActivityDefinition] = deriveEncoder[ActivityDefinition].mapJson(_.dropNullValues)
}
