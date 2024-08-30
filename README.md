# xAPI Scala

[![Build Status](https://github.com/integralla/xapi-scala/actions/workflows/scala.yml/badge.svg)](https://github.com/integralla/xapi-scala/actions/workflows/scala.yml)

## Description

Scala library for creating and working with Experience API (xAPI) resources (such as statements
and documents). 

> The [Experience API (xAPI)](https://xapi.ieee-saopen.org/) is a standard that describes an
> interoperable means to document and communicate information about learning experiences. It
> specifies a structure to describe learning experiences and defines how these descriptions can be
> exchanged electronically.

The library has been cross-built for Scala 3 and Scala 2.13, and is available
from [Maven Central](https://central.sonatype.com/search?namespace=io.integralla&q=xapi-scala).

```text
libraryDependencies += "io.integralla" %% "xapi-scala" % "1.0.0"
```

## Basic Usage

### Statement Creation

Create a basic statement with an explicit identifier:

```scala
import io.integralla.xapi.model._

val statement = Statement(
  id = Some(UUID.fromString("12345678-1234-5678-1234-567812345678")),
  actor = Agent(
    objectType = Some(StatementObjectType.Agent),
    mbox = Some(MBox("mailto:demo@example.com"))
  ),
  verb = StatementVerb(
    id = IRI("http://adlnet.gov/expapi/verbs/created"),
    display = Some(LanguageMap(Map("en-US" -> "created")))
  ),
  `object` = StatementObject(
    Activity(None, IRI("http://example.adlnet.gov/xapi/example/activity"), None)
  )
)
```

### Encoding / Decoding

Encode the statement as JSON, using the `toJson` method:

```scala
val encoded: String = statement.toJson(spaces = true)
```

```json

{
  "actor": {
    "objectType": "Agent",
    "mbox": "mailto:demo@example.com"
  },
  "verb": {
    "id": "http://adlnet.gov/expapi/verbs/created",
    "display": {
      "en-US": "created"
    }
  },
  "object": {
    "id": "http://example.adlnet.gov/xapi/example/activity"
  }
}
```

Decode a JSON representation of a statement, using an `apply` method:

```scala
val decoded: Try[Statement] = Statement(encoded)
assert(decoded.isSuccess)
assert(decoded.get == statement)
```

## Notable Features

### Encoding / Decoding

A `Statement` and every other data type supported by the model can be encoded as a JSON string using
a built-in method called `toJson` as demonstrated above in the "Basic Usage" section.

Similarly, every supported data type supports decoding from JSON using an `apply` method that is
made available via a companion object to the data type case class or enum.

This library uses [circe](https://circe.github.io/circe/), for encoding / decoding. Semi-automatic
derivation is used in most cases, with custom codecs used for complex scenarios.

### Statement Validation

Upon object creation (including decoding), a number of validations are performed to ensure that the
statement is structurally correct, and that properties that can / should be parsed and handled as a
specific reference type (for example, an IRI) can be parsed and handled as such.

If validation fails, a `StatementValidationException` will be thrown with an explanatory message
indicating why validation failed.

JSON schema validation is assumed for basic validation needs, including pattern matching against
strings for such things as UUIDs, hashes, etc.

### Logical Equivalence Testing

The xAPI specification requires the ability to test statements for logical equivalence in certain
contexts such as ensuring statement immutability and verifying statement signatures. This can be
accomplished using an `isEquivalentTo` method available for `Statement` data type (and most others).

```scala
val left: Statement = ???
val right: Statement = ???
assert(left.isEquivalentTo(right))
```

### Listing Agent / Activity References

Filtering statements based upon whether they reference an agent and/or activity can become complex
due to the fact that either object type can be referenced in multiple places. This gets even more
difficult when dealing with sub-statements. In order to make all of this easier, the `Statement`
data type supports methods to retrieve a list of agent references, or a list of activity references,
where the reference data type provides context to facilitate filtering requirements.

* `activityReferences: List[ActivityReference]` – _Extracts and returns all activities (if any)
  referenced by the statement_
* `agentReferences: List[AgentReference]` – _A list of agent references across all parts of the
  statement_

For an activity, the reference model defines the type of reference (for example, as a statement
object or
a parent, grouping, category, or other context activity), as well as a property to indicate whether
the reference occurs in a sub-statement.

For an agent, the reference model defines the type of reference (for example, an actor, object,
authority,
instructor, team), whether the reference occurs in a sub-statement, and whether it is as a
standalone agent or as member of a group.

### Agent / Group Identifiers Helpers

The xAPI specification defines four types of Inverse Functional Identifiers (IFI) to uniquely
identify agents or identified groups within a statement. The library offers a couple of methods to
simplify working with these identifiers:

* `ifiType: Option[String]` – _The IFI type name (for example, `account`)_
* `ifiValue: Option[String]` – _The IFI value as a string (for
  example, `http://www.example.com#123456`)_
* `ifiKey: Option[String]` – _An IFI key composed of its type and value (for
  example, `account#http://www.example.com#123456`)_

The `ifiKey` method is particularly useful for filtering or grouping by agent.

### Statement Manipulation

On decoding statements, we've chosen to keep the modifications to the minimum required for
validation, deferring all other necessary manipulations to downstream processing such as would
typically occur before persistence (for example, adding an identifier or setting the `stored`
timestamp property).

The only mutation made on decoding is to change the `context.contextActivities` property to an array
if it is set to a single `Activity` object (something allowed for backwards compatibility only).

### Other

Please consult the source code, which is well documented, for additional features / details.