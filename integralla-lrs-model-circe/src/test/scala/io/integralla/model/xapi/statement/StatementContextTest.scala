package io.integralla.model.xapi.statement

import io.circe.Json
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.identifiers.{Account, IRI, MBox}
import io.integralla.testing.spec.UnitSpec

import java.util.UUID
import scala.io.Source
import scala.util.Using

class StatementContextTest extends UnitSpec {

  val sampleInstructor: Agent = Agent(
    Some(StatementObjectType.Agent),
    Some("Andrew Downes"),
    None,
    None,
    None,
    Some(Account("http://www.example.com", "13936749"))
  )
  val sampleTeam: Group =
    Group(StatementObjectType.Group, Some("Team PB"), Some(MBox("mailto:teampb@example.com")), None, None, None, None)

  val sampleParent: List[Activity] = List(
    Activity(Some(StatementObjectType.Activity), IRI("http://www.example.com/meetings/series/267"), None)
  )

  val sampleCategory: List[Activity] = List(
    Activity(
      Some(StatementObjectType.Activity),
      IRI("http://www.example.com/meetings/categories/teammeeting"),
      Some(
        ActivityDefinition(
          Some(Map("en" -> "team meeting")),
          Some(Map("en" -> "A category of meeting used for regular team meetings.")),
          Some(IRI("http://example.com/expapi/activities/meetingcategory")),
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )
      )
    )
  )

  val sampleOther: List[Activity] = List(
    Activity(Some(StatementObjectType.Activity), IRI("http://www.example.com/meetings/occurances/34257"), None),
    Activity(Some(StatementObjectType.Activity), IRI("http://www.example.com/meetings/occurances/3425567"), None)
  )

  val sampleContextActivities: ContextActivities = ContextActivities(
    Some(sampleParent),
    None,
    Some(sampleCategory),
    Some(sampleOther)
  )

  val sampleStatementRef: StatementRef =
    StatementRef(StatementObjectType.StatementRef, UUID.fromString("6690e6c9-3ef0-4ed3-8b37-7f3964730bee"))

  val sampleExtensionValue: Json = Map("name" -> "Kilby", "id" -> "http://example.com/rooms/342").asJson
  val sampleExtensions: ExtensionMap = ExtensionMap(
    Map(
      IRI("http://example.com/profiles/meetings/activitydefinitionextensions/room") -> sampleExtensionValue
    )
  )

  val sampleContext: StatementContext = StatementContext(
    Some(UUID.fromString("ec531277-b57b-4c15-8d91-d292c5b2b8f7")),
    Some(sampleInstructor),
    Some(sampleTeam),
    Some(sampleContextActivities),
    Some("1.0.0"),
    Some("Example virtual meeting software"),
    Some("tlh"),
    Some(sampleStatementRef),
    Some(sampleExtensions)
  )

  val sampleContextEncoded: String = Using.resource(Source.fromResource("data/sample-context-object.json"))(_.mkString)

  describe("Statement Context") {
    describe("[encoding]") {
      it("should successfully encode a context object") {
        val actual: String = sampleContext.asJson.spaces2
        assert(actual === sampleContextEncoded)
      }
    }

    describe("[decoding]") {
      it("should successfully decode a context object") {
        val decoded: Either[io.circe.Error, StatementContext] = decode[StatementContext](sampleContextEncoded)
        decoded match {
          case Right(actual) => assert(actual === sampleContext)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }
    }
  }
}
