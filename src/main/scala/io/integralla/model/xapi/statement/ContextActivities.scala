package io.integralla.model.xapi.statement

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import io.integralla.model.references.{ActivityReference, CategoryRef, GroupingRef, OtherRef, ParentRef}
import io.integralla.model.xapi.common.Equivalence

/** A map of the types of learning activity context that the statement is
  * related to
  *
  * @param parent
  *   An activity with a direct relation to the activity which is the object of
  *   the statement
  * @param grouping
  *   An activity with an indirect relation to the activity which is the object
  *   of the statement
  * @param category
  *   An activity used to categorize the statement
  * @param other
  *   A context activity that doesn't fit one of the other properties
  */
case class ContextActivities(
  parent: Option[List[Activity]],
  grouping: Option[List[Activity]],
  category: Option[List[Activity]],
  other: Option[List[Activity]]
) extends Equivalence {

  /** @return A distinct list of all activities referenced in the context */
  def getActivityReferences(inSubStatement: Boolean = false): List[ActivityReference] = {
    List(
      parent.map(_.map(activity => ActivityReference(activity, ParentRef, inSubStatement))),
      grouping.map(_.map(activity => ActivityReference(activity, GroupingRef, inSubStatement))),
      category.map(_.map(activity => ActivityReference(activity, CategoryRef, inSubStatement))),
      other.map(_.map(activity => ActivityReference(activity, OtherRef, inSubStatement)))
    ).flatMap(_.getOrElse(List.empty[ActivityReference])).distinct
  }

  /** Generates a signature that can be used to test logical equivalence between
    * objects
    *
    * The signature for context activities is computed by extracting the
    * activity signature for each activity, in each context classification,
    * sorting them, and then concatenating the results together. The resulting
    * strings generated for each classification are then concatenated themselves
    * and hashed
    *
    * @return
    *   A string identifier
    */
  override protected[xapi] def signature(): String = {
    hash {
      combine {
        List(
          parent.map(activities => activities.map(_.signature()).sorted.mkString(separator)).getOrElse(placeholder),
          grouping.map(activities => activities.map(_.signature()).sorted.mkString(separator)).getOrElse(placeholder),
          category.map(activities => activities.map(_.signature()).sorted.mkString(separator)).getOrElse(placeholder),
          other.map(activities => activities.map(_.signature()).sorted.mkString(separator)).getOrElse(placeholder)
        )
      }
    }
  }
}

object ContextActivities {

  /** For backwards compatibility, the specification allows an activity provider
    * to set each of the properties to a single activity object rather than an
    * array of activity objects. The LRS, however, is expected to wrap a single
    * activity object in an array and return it as such
    */
  implicit val decoder: Decoder[ContextActivities] = (c: HCursor) => {
    for {
      parent <- c
        .downField("parent").withFocus((json: Json) => {
          if (json.isArray) json else List(json).asJson
        }).as[Option[List[Activity]]]

      grouping <- c
        .downField("grouping").withFocus((json: Json) => {
          if (json.isArray) json else List(json).asJson
        }).as[Option[List[Activity]]]

      category <- c
        .downField("category").withFocus((json: Json) => {
          if (json.isArray) json else List(json).asJson
        }).as[Option[List[Activity]]]

      other <- c
        .downField("other").withFocus((json: Json) => {
          if (json.isArray) json else List(json).asJson
        }).as[Option[List[Activity]]]

    } yield {
      ContextActivities(parent, grouping, category, other)
    }
  }

  implicit val encoder: Encoder[ContextActivities] = deriveEncoder[ContextActivities].mapJson(_.dropNullValues)
}
