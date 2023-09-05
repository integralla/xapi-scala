package io.integralla.model.references

/** An ADT to represent the ways in which an activity can be referenced within a
  * statement
  */
sealed trait ActivityReferenceType

/** When the activity is the object of the statement */
case object ActivityObjectRef extends ActivityReferenceType

/** When the activity is a parent activity within the statement context */
case object ParentRef extends ActivityReferenceType

/** When the activity is a grouping activity within the statement context */
case object GroupingRef extends ActivityReferenceType

/** When the activity is a category activity within the statement context */
case object CategoryRef extends ActivityReferenceType

/** When the activity is an other activity within the statement context */
case object OtherRef extends ActivityReferenceType
