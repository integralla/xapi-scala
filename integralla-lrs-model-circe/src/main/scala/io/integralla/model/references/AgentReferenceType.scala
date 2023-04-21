package io.integralla.model.references

/** An ADT to represent the ways in which an agent (or identified group) can be referenced within a statement
  *
  * Note that an agent can be referenced in any of these positions either as a standalone agent or as a member of
  * a group
  */
sealed trait AgentReferenceType

/** When the agent is the statement actor */
case object ActorRef extends AgentReferenceType

/** When the agent is the statement object */
case object AgentObjectRef extends AgentReferenceType

/** When the agent is the statement authority */
case object AuthorityRef extends AgentReferenceType

/** When the agent is the statement instructor */
case object InstructorRef extends AgentReferenceType

/** When the agent is the statement team */
case object TeamRef extends AgentReferenceType
