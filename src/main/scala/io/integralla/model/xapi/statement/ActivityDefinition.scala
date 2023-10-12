package io.integralla.model.xapi.statement

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.integralla.model.xapi.common.{Equivalence, ExtensionMap}
import io.integralla.model.xapi.identifiers.IRI
import io.integralla.model.xapi.statement.InteractionType.{
  CHOICE,
  FILL_IN,
  InteractionType,
  LIKERT,
  LONG_FILL_IN,
  MATCHING,
  NUMERIC,
  OTHER,
  PERFORMANCE,
  SEQUENCING,
  TRUE_FALSE
}
import io.lemonlabs.uri.AbsoluteUrl

import scala.util.{Failure, Success}

/** The definition of an activity object
  *
  * @param name
  *   A Language Map that provides the human readable/visual name of the
  *   activity
  * @param description
  *   A Language Map that provides a description of the activity
  * @param `type`
  *   An IRI that uniquely identifies the type of activity
  * @param moreInfo
  *   An IRI that resolves to a document with human-readable information about
  *   the Activity, which could include a way to launch the activity
  * @param interactionType
  *   The type of interaction for a traditional e-learning activity (for
  *   example, choice, true-false, matching)
  * @param correctResponsesPattern
  *   A pattern representing the correct response to the interaction
  * @param choices
  *   An interaction component that defines a list of the options available in
  *   the interaction for selection or ordering
  * @param scale
  *   An interaction component that defines a list of the options on the likert
  *   scale
  * @param source
  *   An interaction component that defines a list of sources to be matched
  *   against targets
  * @param steps
  *   An interaction component that defines a list of the elements making up the
  *   performance interaction
  * @param target
  *   An interaction component that defines a list of targets against which
  *   sources can be matched
  * @param extensions
  *   A map of other properties as needed
  */
case class ActivityDefinition(
  name: Option[LanguageMap] = None,
  description: Option[LanguageMap] = None,
  `type`: Option[IRI] = None,
  moreInfo: Option[IRI] = None,
  interactionType: Option[InteractionType] = None,
  correctResponsesPattern: Option[CorrectResponsePattern] = None,
  choices: Option[List[InteractionComponent]] = None,
  scale: Option[List[InteractionComponent]] = None,
  source: Option[List[InteractionComponent]] = None,
  steps: Option[List[InteractionComponent]] = None,
  target: Option[List[InteractionComponent]] = None,
  extensions: Option[ExtensionMap] = None
) extends StatementValidation with Equivalence {

  /** Computes whether this activity definition is compatible with another
    * instance
    *
    * Compatibility is based solely on interaction properties which include the
    * interaction type, the correct response pattern and set of possible
    * interaction component lists (choices, scale, source, steps, target)
    *
    * Definitions are considered as compatible if the interaction properties are
    * logically equivalent. For example, an activity definition for a multiple
    * choice activity would be considered compatible so long as the choices
    * remained the same, regardless of the order in which they are defined. The
    * definitions of interaction components are not included in the comparison.
    * For example, if the text for a given choice in the multiple choice
    * activity changes, or if a new language map is added, it would still be
    * considered compatible
    *
    * @param instance
    *   The instance to compare this activity definition with
    * @return
    *   True if this instance is compatible with the provided instance
    */
  def isCompatibleWith(instance: ActivityDefinition): Boolean = {

    /** Computes compatibility based on the interaction type
      *
      * The interaction type is treated as compatible if it is undefined on the
      * compared instance or if the interaction type for both instances is the
      * same
      *
      * @return
      *   True if the interaction type is compatible, else false
      */
    def interactionTypeIsCompatible(): Boolean = {
      if (instance.interactionType.isDefined) {
        if (this.interactionType == instance.interactionType) true else false
      } else true
    }

    /** Computes compatibility based on the correct response pattern
      *
      * The correct response pattern is treated as compatible if it is undefined
      * on the compared instance or if the correct response patterns for both
      * are logically equivalent
      *
      * @return
      *   True if the correct response pattern is compatible, else false
      */
    def correctResponsePatternIsCompatible(): Boolean = {
      if (instance.correctResponsesPattern.isDefined) {
        this.correctResponsesPattern.exists(pattern => pattern.isEquivalentTo(instance.correctResponsesPattern.get))
      } else true
    }

    /** Computes compatibility based on the interaction components
      *
      * The set of possible interaction components are treated as compatible if
      * none are defined on the compared instance, or if the set of interaction
      * components identifiers (`id`) are same
      *
      * The set of interaction component identifiers are compared by adding them
      * to a list, sorting them, and concatenating them with a standard
      * separator.
      *
      * The interaction component definitions are not considered because they
      * can be changed without breaking backwards compatibility (for example, a
      * spelling error can be fixed in the text of a multiple choice option, or
      * a new language map can be added)
      *
      * @return
      *   True of the set of possible interaction components are equivalent
      */
    def interactionComponentsAreCompatible(): Boolean = {

      def compareInteractionComponents(
        left: Option[List[InteractionComponent]],
        right: Option[List[InteractionComponent]]
      ): Boolean = {
        left.map(_.map(_.id).sorted.mkString("#")) == right.map(_.map(_.id).sorted.mkString("#"))
      }

      if (
        List(instance.choices, instance.scale, instance.source, instance.target, instance.steps).exists(_.isDefined)
      ) {
        List(
          compareInteractionComponents(this.choices, instance.choices),
          compareInteractionComponents(this.scale, instance.scale),
          compareInteractionComponents(this.source, instance.source),
          compareInteractionComponents(this.target, instance.target),
          compareInteractionComponents(this.steps, instance.steps)
        ).forall(_ == true)
      } else true
    }

    List(
      interactionTypeIsCompatible(),
      correctResponsePatternIsCompatible(),
      interactionComponentsAreCompatible()
    ).forall(_ == true)
  }

  override def validate: Seq[Either[String, Boolean]] = {
    Seq(
      validateInteractionTypeByType,
      validateInteractionTypeByCorrectResponsePattern,
      validateInteractionTypeByInteractionComponents,
      validateInteractionComponentByInteractionType,
      validateMoreInfoIRL
    )
  }

  private def validateInteractionTypeByType: Either[String, Boolean] = {
    val interactionActivityType: String = "http://adlnet.gov/expapi/activities/cmi.interaction"
    `type`
      .filter(_.value == interactionActivityType)
      .map(_ => {
        interactionType match {
          case Some(_) => Right(true)
          case None    => Left("An interaction type must be specified when the activity type is cmi.interaction")
        }
      }).getOrElse(Right(true))
  }

  private def validateInteractionTypeByCorrectResponsePattern: Either[String, Boolean] = {
    correctResponsesPattern
      .map(_ => {
        interactionType match {
          case Some(_) => Right(true)
          case None    => Left("An interaction type must be specified when a correct response pattern is defined")
        }
      }).getOrElse(Right(true))
  }

  private def validateInteractionTypeByInteractionComponents: Either[String, Boolean] = {
    if (List(choices, scale, source, steps, target).exists(_.isDefined)) {
      interactionType match {
        case Some(_) => Right(true)
        case None    => Left("An interaction type must be specified when interaction components are defined")
      }
    } else Right(true)
  }

  private def validateInteractionComponentByInteractionType: Either[String, Boolean] = {

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

  private def validateMoreInfoIRL: Either[String, Boolean] = {
    moreInfo
      .map((iri: IRI) => {
        AbsoluteUrl.parseTry(iri.value) match {
          case Failure(exception) => Left(f"The value of moreInfo must be a valid IRL: ${exception.getMessage}")
          case Success(_)         => Right(true)
        }
      }).getOrElse(Right(true))
  }

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * The signature for the activity definition is computed by extracting a
    * signature from each property, or a placeholder value, and then
    * concatenating with a standard separator and hashing as usual
    *
    * For list of interaction components, a signature is extracted from each
    * component, and then those are sorted, combined, and hashed as usual
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      combine {
        List(
          name.map(_.signature()).getOrElse(placeholder),
          description.map(_.signature()).getOrElse(placeholder),
          `type`.map(_.signature()).getOrElse(placeholder),
          moreInfo.map(_.signature()).getOrElse(placeholder),
          interactionType.map(_.toString).getOrElse(placeholder),
          correctResponsesPattern.map(_.signature()).getOrElse(placeholder),
          choices.map(choices => signatureFromList(choices)).getOrElse(placeholder),
          scale.map(scale => signatureFromList(scale)).getOrElse(placeholder),
          source.map(source => signatureFromList(source)).getOrElse(placeholder),
          steps.map(steps => signatureFromList(steps)).getOrElse(placeholder),
          target.map(target => signatureFromList(target)).getOrElse(placeholder),
          extensions.map(_.signature()).getOrElse(placeholder)
        )
      }
    }
  }
}

object ActivityDefinition {
  implicit val decoder: Decoder[ActivityDefinition] = deriveDecoder[ActivityDefinition]
  implicit val encoder: Encoder[ActivityDefinition] = deriveEncoder[ActivityDefinition].mapJson(_.dropNullValues)
}
