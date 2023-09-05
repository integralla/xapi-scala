package io.integralla.model.references

import io.integralla.model.xapi.statement.Activity

/** Model for an activity reference within a statement
  *
  * @param activity
  *   An Activity
  * @param referenceType
  *   The type of reference (object, parent, grouping, category, other)
  * @param inSubStatement
  *   Whether the reference occurs in a sub-statement
  */
case class ActivityReference(
  activity: Activity,
  referenceType: ActivityReferenceType,
  inSubStatement: Boolean
)
