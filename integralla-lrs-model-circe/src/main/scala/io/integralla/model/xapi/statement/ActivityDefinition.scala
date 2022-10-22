package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.statement.InteractionType.InteractionType
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.IRI
import io.lemonlabs.uri.AbsoluteUrl

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
) extends StatementModelValidation {

  override def validate(): Unit = {
    validateInteractionTypeByType()
    validateInteractionTypeByCorrectResponsePattern()
    validateInteractionTypeByInteractionComponents()
    validateInteractionComponentByInteractionType()
    validateMoreInfoIRL()
  }

  def validateInteractionTypeByType(): Unit = {
    val interactionActivityType: String = "http://adlnet.gov/expapi/activities/cmi.interaction"
    `type`.filter(_.value == interactionActivityType)
      .foreach(_ => {
        if (interactionType.isEmpty) {
          throw new StatementValidationException("An interaction type must be specified when the activity type is cmi.interaction")
        }
      })
  }

  def validateInteractionTypeByCorrectResponsePattern(): Unit = {
    if (correctResponsesPattern.isDefined) {
      if (interactionType.isEmpty) {
        throw new StatementValidationException("An interaction type must be specified when a correct response pattern is defined")
      }
    }
  }

  def validateInteractionTypeByInteractionComponents(): Unit = {
    if (List(choices, scale, source, steps, target).exists(_.isDefined)) {
      if (interactionType.isEmpty) {
        throw new StatementValidationException("An interaction type must be specified when interaction components are defined")
      }
    }
  }

  def validateInteractionComponentByInteractionType(): Unit = {
    if (choices.isDefined) {
      interactionType.foreach((interactionType: InteractionType) => {
        if (!List(InteractionType.CHOICE, InteractionType.SEQUENCING).contains(interactionType)) {
          throw new StatementValidationException("The interaction component type of \"choice\" is only supported by for the choice or sequencing interaction types")
        }
      })
    }

    if (scale.isDefined) {
      interactionType.foreach((interactionType: InteractionType) => {
        if (interactionType != InteractionType.LIKERT) {
          throw new StatementValidationException("The interaction component type of \"scale\" is only supported by the likert interaction type")
        }
      })
    }

    if (source.isDefined) {
      interactionType.foreach((interactionType: InteractionType) => {
        if (interactionType != InteractionType.MATCHING) {
          throw new StatementValidationException("The interaction component type of \"source\" is only supported by the matching interaction type")
        }
      })
    }

    if (target.isDefined) {
      interactionType.foreach((interactionType: InteractionType) => {
        if (interactionType != InteractionType.MATCHING) {
          throw new StatementValidationException("The interaction component type of \"target\" is only supported by the matching interaction type")
        }
      })
    }

    if (steps.isDefined) {
      interactionType.foreach((interactionType: InteractionType) => {
        if (interactionType != InteractionType.PERFORMANCE) {
          throw new StatementValidationException("The interaction component type of \"steps\" is only supported by the performance interaction type")
        }
      })
    }
  }

  def validateMoreInfoIRL(): Unit = {
    moreInfo.foreach((iri: IRI) => {
      try {
        AbsoluteUrl.parse(iri.value)
      } catch {
        case _: Throwable => throw new StatementValidationException("The value of moreInfo must be a valid IRL")
      }
    })
  }
}

object ActivityDefinition {
  implicit val decoder: Decoder[ActivityDefinition] = deriveDecoder[ActivityDefinition]
  implicit val encoder: Encoder[ActivityDefinition] = deriveEncoder[ActivityDefinition].mapJson(_.dropNullValues)
}
