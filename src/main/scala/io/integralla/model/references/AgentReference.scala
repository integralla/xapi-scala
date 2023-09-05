package io.integralla.model.references

import io.integralla.model.xapi.statement.StatementActor

/** Model for an agent reference within a statement
  *
  * @param agent
  *   A StatementActor (agent or identified group)
  * @param referenceType
  *   The type of reference (actor, object, authority, instructor, team)
  * @param inSubStatement
  *   Whether the reference occurs in a sub-statement
  * @param asGroupMember
  *   Whether the agent is referenced as a group member
  */
case class AgentReference(
  agent: StatementActor,
  referenceType: AgentReferenceType,
  inSubStatement: Boolean,
  asGroupMember: Boolean
)
