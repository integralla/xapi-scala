package io.integralla.model.xapi.statement

import io.circe.jawn.decode
import io.circe.parser
import io.circe.syntax.EncoderOps
import io.integralla.model.xapi.statement.exceptions.StatementValidationException
import io.integralla.model.xapi.statement.identifiers.IRI
import io.integralla.testing.spec.UnitSpec

class ActivityDefinitionTest extends UnitSpec {

  /* Shared */
  val nameLanguageMap: LanguageMap = Map("en-US" -> "Example Activity", "it-IT" -> "Esempio di attività")
  val descriptionLanguageMap: LanguageMap = Map("en-US" -> "An xAPI activity", "it-IT" -> "Un'attività xAPI")
  val interactionActivityType: IRI = IRI("http://adlnet.gov/expapi/activities/cmi.interaction")
  val moreInfo: IRI = IRI("https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C")
  val extensions: ExtensionMap = ExtensionMap(
    Map(
      IRI("http://example.com/extenions/boolean") -> true.asJson,
      IRI("http://example.com/extenions/double") -> 1.0.asJson,
      IRI("http://example.com/extenions/string") -> "string".asJson,
      IRI("http://example.com/extenions/other") -> parser.parse("""{"one": 1, "two": 2}""").toOption.get
    )
  )

  /* Non-Interaction Activity */
  val nonInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    None,
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
  val nonInteractionActivityEncoded: String =
    """{"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"}}"""

  /* Non-Interaction Activity w/ Extensions */
  val activityWithExtensions: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    Some(extensions)
  )
  val activityWithExtensionsEncoded: String =
    """{"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},"extensions":{"http://example.com/extenions/boolean":true,"http://example.com/extenions/double":1.0,"http://example.com/extenions/string":"string","http://example.com/extenions/other":{"one":1,"two":2}}}"""

  /* Choice Interaction Activity */
  val choiceCorrectResponsePattern: CorrectResponsePattern = CorrectResponsePattern(List("golf[,]tetris"))
  val choiceInteractionComponents: List[InteractionComponent] = List(
    InteractionComponent("golf", Some(Map("en-US" -> "Golf Example"))),
    InteractionComponent("facebook", Some(Map("en-US" -> "Facebook App"))),
    InteractionComponent("tetris", Some(Map("en-US" -> "Tetris Example"))),
    InteractionComponent("scrabble", Some(Map("en-US" -> "Scrabble Example")))
  )
  val choiceInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.CHOICE),
    Some(choiceCorrectResponsePattern),
    Some(choiceInteractionComponents),
    None,
    None,
    None,
    None,
    None
  )
  val choiceInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"choice",
      |"correctResponsesPattern":["golf[,]tetris"],
      |"choices":[{"id":"golf","definition":{"en-US":"Golf Example"}},{"id":"facebook","definition":{"en-US":"Facebook App"}},{"id":"tetris","definition":{"en-US":"Tetris Example"}},{"id":"scrabble","definition":{"en-US":"Scrabble Example"}}]
      |}""".stripMargin

  /* Fill-In Interaction Activity */
  val fillInCorrectResponsesPattern: CorrectResponsePattern = CorrectResponsePattern(List("Lorum ipsum"))
  val fillInInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.FILL_IN),
    Some(fillInCorrectResponsesPattern),
    None,
    None,
    None,
    None,
    None,
    None
  )
  val fillInInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"fill-in",
      |"correctResponsesPattern":["Lorum ipsum"]
      |}""".stripMargin

  /* Likert Interaction Activity */
  val likertCorrectResponsePattern: CorrectResponsePattern = CorrectResponsePattern(List("likert_3"))
  val likertInteractionComponents: List[InteractionComponent] = List(
    InteractionComponent("likert_0", Some(Map("en-US" -> "It's OK"))),
    InteractionComponent("likert_1", Some(Map("en-US" -> "It's Pretty Cool"))),
    InteractionComponent("likert_2", Some(Map("en-US" -> "It's Damn Cool"))),
    InteractionComponent("likert_3", Some(Map("en-US" -> "It's Gonna Change the World")))
  )
  val likertInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.LIKERT),
    Some(likertCorrectResponsePattern),
    None,
    Some(likertInteractionComponents),
    None,
    None,
    None,
    None
  )
  val likertInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"likert",
      |"correctResponsesPattern":["likert_3"],
      |"scale":[{"id":"likert_0","definition":{"en-US":"It's OK"}},{"id":"likert_1","definition":{"en-US":"It's Pretty Cool"}},{"id":"likert_2","definition":{"en-US":"It's Damn Cool"}},{"id":"likert_3","definition":{"en-US":"It's Gonna Change the World"}}]
      |}""".stripMargin

  /* Long Fill-In Interaction Activity */
  val longFillInCorrectResponsesPattern: CorrectResponsePattern = CorrectResponsePattern(
    List(
      "{case_matters=false}{lang=en}The purpose of the xAPI is to store and provide access to learning experiences."
    )
  )
  val longFillInInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.LONG_FILL_IN),
    Some(longFillInCorrectResponsesPattern),
    None,
    None,
    None,
    None,
    None,
    None
  )
  val longFillInInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"long-fill-in",
      |"correctResponsesPattern":["{case_matters=false}{lang=en}The purpose of the xAPI is to store and provide access to learning experiences."]
      |}""".stripMargin

  /* Matching Interaction Activity */
  val matchingCorrectResponsePattern: CorrectResponsePattern = CorrectResponsePattern(
    List("ben[.]3[,]chris[.]2[,]troy[.]4[,]freddie[.]1")
  )
  val matchingInteractionComponentsSource: List[InteractionComponent] = List(
    InteractionComponent("ben", Some(Map("en-US" -> "Ben"))),
    InteractionComponent("chris", Some(Map("en-US" -> "Chris"))),
    InteractionComponent("troy", Some(Map("en-US" -> "Troy"))),
    InteractionComponent("freddie", Some(Map("en-US" -> "Freddie")))
  )
  val matchingInteractionComponentsTarget: List[InteractionComponent] = List(
    InteractionComponent("1", Some(Map("en-US" -> "Swift Kick in the Grass"))),
    InteractionComponent("2", Some(Map("en-US" -> "We got Runs"))),
    InteractionComponent("3", Some(Map("en-US" -> "Duck"))),
    InteractionComponent("4", Some(Map("en-US" -> "Van Delay Industries")))
  )
  val matchingInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.MATCHING),
    Some(matchingCorrectResponsePattern),
    None,
    None,
    Some(matchingInteractionComponentsSource),
    None,
    Some(matchingInteractionComponentsTarget),
    None
  )
  val matchingInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"matching",
      |"correctResponsesPattern":["ben[.]3[,]chris[.]2[,]troy[.]4[,]freddie[.]1"],
      |"source":[{"id":"ben","definition":{"en-US":"Ben"}},{"id":"chris","definition":{"en-US":"Chris"}},{"id":"troy","definition":{"en-US":"Troy"}},{"id":"freddie","definition":{"en-US":"Freddie"}}],
      |"target":[{"id":"1","definition":{"en-US":"Swift Kick in the Grass"}},{"id":"2","definition":{"en-US":"We got Runs"}},{"id":"3","definition":{"en-US":"Duck"}},{"id":"4","definition":{"en-US":"Van Delay Industries"}}]
      |}""".stripMargin

  /* Numeric Interaction Activity */
  val numericCorrectResponsesPattern: CorrectResponsePattern = CorrectResponsePattern(List("4[:]"))
  val numericInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.NUMERIC),
    Some(numericCorrectResponsesPattern),
    None,
    None,
    None,
    None,
    None,
    None
  )
  val numericInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"numeric",
      |"correctResponsesPattern":["4[:]"]
      |}""".stripMargin

  /* Other Interaction Activity */
  val otherCorrectResponsesPattern: CorrectResponsePattern = CorrectResponsePattern(List("(35.937432,-86.868896)"))
  val otherInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.OTHER),
    Some(otherCorrectResponsesPattern),
    None,
    None,
    None,
    None,
    None,
    None
  )
  val otherInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"other",
      |"correctResponsesPattern":["(35.937432,-86.868896)"]
      |}""".stripMargin

  /* Performance Interaction Activity */
  val performanceCorrectResponsePattern: CorrectResponsePattern = CorrectResponsePattern(
    List("pong[.]1:[,]dg[.]:10[,]lunch[.]")
  )
  val performanceInteractionComponents: List[InteractionComponent] = List(
    InteractionComponent("pong", Some(Map("en-US" -> "Net pong matches won"))),
    InteractionComponent("dg", Some(Map("en-US" -> "Strokes over par in disc golf at Liberty"))),
    InteractionComponent("lunch", Some(Map("en-US" -> "Lunch having been eaten")))
  )
  val performanceInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.PERFORMANCE),
    Some(performanceCorrectResponsePattern),
    None,
    None,
    None,
    Some(performanceInteractionComponents),
    None,
    None
  )
  val performanceInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"performance",
      |"correctResponsesPattern":["pong[.]1:[,]dg[.]:10[,]lunch[.]"],
      |"steps":[{"id":"pong","definition":{"en-US":"Net pong matches won"}},{"id":"dg","definition":{"en-US":"Strokes over par in disc golf at Liberty"}},{"id":"lunch","definition":{"en-US":"Lunch having been eaten"}}]
      |}""".stripMargin

  /* Sequencing Interaction Activity */
  val sequencingCorrectResponsePattern: CorrectResponsePattern = CorrectResponsePattern(List("tim[,]mike[,]ells[,]ben"))
  val sequencingInteractionComponents: List[InteractionComponent] = List(
    InteractionComponent("tim", Some(Map("en-US" -> "Tim"))),
    InteractionComponent("ben", Some(Map("en-US" -> "Ben"))),
    InteractionComponent("ells", Some(Map("en-US" -> "Ells"))),
    InteractionComponent("mike", Some(Map("en-US" -> "Mike")))
  )
  val sequencingInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.SEQUENCING),
    Some(sequencingCorrectResponsePattern),
    Some(sequencingInteractionComponents),
    None,
    None,
    None,
    None,
    None
  )
  val sequencingInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"sequencing",
      |"correctResponsesPattern":["tim[,]mike[,]ells[,]ben"],
      |"choices":[{"id":"tim","definition":{"en-US":"Tim"}},{"id":"ben","definition":{"en-US":"Ben"}},{"id":"ells","definition":{"en-US":"Ells"}},{"id":"mike","definition":{"en-US":"Mike"}}]
      |}""".stripMargin

  /* True/False Interaction Activity */
  val trueFalseCorrectResponsesPattern: CorrectResponsePattern = CorrectResponsePattern(List("true"))
  val trueFalseInteractionActivity: ActivityDefinition = ActivityDefinition(
    Some(nameLanguageMap),
    Some(descriptionLanguageMap),
    Some(interactionActivityType),
    Some(moreInfo),
    Some(InteractionType.TRUE_FALSE),
    Some(trueFalseCorrectResponsesPattern),
    None,
    None,
    None,
    None,
    None,
    None
  )
  val trueFalseInteractionActivityEncoded: String =
    """{
      |"name":{"en-US":"Example Activity","it-IT":"Esempio di attività"},
      |"description":{"en-US":"An xAPI activity","it-IT":"Un'attività xAPI"},
      |"type":"http://adlnet.gov/expapi/activities/cmi.interaction",
      |"moreInfo":"https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#Appendix2C",
      |"interactionType":"true-false",
      |"correctResponsesPattern":["true"]
      |}""".stripMargin

  describe("ActivityDefinition") {
    describe("[initialization / validation]") {
      it("should initialize an activity") {
        val activityDefinition: ActivityDefinition = nonInteractionActivity
        println(activityDefinition)
      }

      it("should initialize an activity with extensions") {
        val activityDefinition: ActivityDefinition = activityWithExtensions
        println(activityDefinition)
      }

      it("should initialize an interaction activity") {
        val activityDefinition: ActivityDefinition = trueFalseInteractionActivity
        println(activityDefinition)
      }

      it(
        "should throw a statement validation exception when the activity type is cmi.interaction and an interaction type is not defined"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            Some(interactionActivityType),
            Some(moreInfo),
            None,
            Some(trueFalseCorrectResponsesPattern),
            None,
            None,
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains(
            "An interaction type must be specified when the activity type is cmi.interaction"
          )
        )
      }

      it(
        "should throw a statement validation exception when a correct response pattern is defined and an interaction type is not defined"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            None,
            Some(choiceCorrectResponsePattern),
            Some(choiceInteractionComponents),
            None,
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains(
            "An interaction type must be specified when a correct response pattern is defined"
          )
        )
      }

      it(
        "should throw a statement validation exception when one or more interaction components are defined and an interaction type is not defined"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            None,
            None,
            Some(choiceInteractionComponents),
            None,
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains("An interaction type must be specified when interaction components are defined")
        )
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (choice)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.CHOICE),
            None,
            None,
            Some(likertInteractionComponents),
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains("The choice interaction type only supports the choices interaction component")
        )
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (sequencing)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.SEQUENCING),
            None,
            None,
            Some(likertInteractionComponents),
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains(
            "The sequencing interaction type only supports the choices interaction component"
          )
        )
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (fill-in)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.FILL_IN),
            None,
            None,
            Some(likertInteractionComponents),
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains("The fill-in interaction type does not support any interaction components")
        )
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (long-fill-in)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.LONG_FILL_IN),
            None,
            None,
            Some(likertInteractionComponents),
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains("The long-fill-in interaction type does not support any interaction components")
        )
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (numeric)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.NUMERIC),
            None,
            None,
            Some(likertInteractionComponents),
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains("The numeric interaction type does not support any interaction components")
        )
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (other)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.OTHER),
            None,
            None,
            Some(likertInteractionComponents),
            None,
            None,
            None,
            None
          )
        }
        assert(exception.getMessage.contains("The other interaction type does not support any interaction components"))
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (true-false)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.TRUE_FALSE),
            None,
            None,
            Some(likertInteractionComponents),
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains("The true-false interaction type does not support any interaction components")
        )
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (likert)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.LIKERT),
            None,
            Some(choiceInteractionComponents),
            None,
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains("The likert interaction type only supports the scale interaction component")
        )
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (matching)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.MATCHING),
            None,
            Some(choiceInteractionComponents),
            None,
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains(
            "The matching interaction type only supports the source and target interaction components"
          )
        )
      }

      it(
        "should throw a statement validation exception when an interaction component is present that is not supported by the interaction type (performance)"
      ) {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(moreInfo),
            Some(InteractionType.PERFORMANCE),
            None,
            Some(choiceInteractionComponents),
            None,
            None,
            None,
            None,
            None
          )
        }
        assert(
          exception.getMessage.contains(
            "The performance interaction type only supports the steps interaction component"
          )
        )
      }

      it("should throw a statement validation exception if the value of moreInfo is not a valid IRL") {
        val exception = intercept[StatementValidationException] {
          ActivityDefinition(
            Some(nameLanguageMap),
            Some(descriptionLanguageMap),
            None,
            Some(IRI("moreInfo:activity:foo")),
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None
          )
        }
        assert(exception.getMessage.contains("The value of moreInfo must be a valid IRL"))
      }
    }

    describe("[encoding]") {
      it("should successfully encode an activity definition") {
        val activityDefinition: ActivityDefinition = nonInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === nonInteractionActivityEncoded)
      }

      it("should successfully encode an activity definition with extensions") {
        val activityDefinition: ActivityDefinition = activityWithExtensions
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === activityWithExtensionsEncoded)
      }

      it("should successfully encode an interaction activity definition (choice)") {
        val activityDefinition: ActivityDefinition = choiceInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === choiceInteractionActivityEncoded.replaceAll("\n", ""))
      }

      it("should successfully encode an interaction activity definition (fill-in)") {
        val activityDefinition: ActivityDefinition = fillInInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === fillInInteractionActivityEncoded.replaceAll("\n", ""))
      }

      it("should successfully encode an interaction activity definition (likert)") {
        val activityDefinition: ActivityDefinition = likertInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === likertInteractionActivityEncoded.replaceAll("\n", ""))
      }

      it("should successfully encode an interaction activity definition (long-fill-in)") {
        val activityDefinition: ActivityDefinition = longFillInInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === longFillInInteractionActivityEncoded.replaceAll("\n", ""))
      }

      it("should successfully encode an interaction activity definition (matching)") {
        val activityDefinition: ActivityDefinition = matchingInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === matchingInteractionActivityEncoded.replaceAll("\n", ""))
      }

      it("should successfully encode an interaction activity definition (numeric)") {
        val activityDefinition: ActivityDefinition = numericInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === numericInteractionActivityEncoded.replaceAll("\n", ""))
      }

      it("should successfully encode an interaction activity definition (other)") {
        val activityDefinition: ActivityDefinition = otherInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === otherInteractionActivityEncoded.replaceAll("\n", ""))
      }

      it("should successfully encode an interaction activity definition (performance)") {
        val activityDefinition: ActivityDefinition = performanceInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === performanceInteractionActivityEncoded.replaceAll("\n", ""))
      }

      it("should successfully encode an interaction activity definition (sequencing)") {
        val activityDefinition: ActivityDefinition = sequencingInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === sequencingInteractionActivityEncoded.replaceAll("\n", ""))
      }

      it("should successfully encode an interaction activity definition (true-false)") {
        val activityDefinition: ActivityDefinition = trueFalseInteractionActivity
        val encoded = activityDefinition.asJson.noSpaces
        assert(encoded === trueFalseInteractionActivityEncoded.replaceAll("\n", ""))
      }
    }

    describe("[decoding]") {
      it("should successfully decode an activity definition") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](nonInteractionActivityEncoded)
        val expected: ActivityDefinition = nonInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode an activity definition with extensions") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](activityWithExtensionsEncoded)
        val expected: ActivityDefinition = activityWithExtensions
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (choice)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](choiceInteractionActivityEncoded)
        val expected: ActivityDefinition = choiceInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (fill-in)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](fillInInteractionActivityEncoded)
        val expected: ActivityDefinition = fillInInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (likert)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](likertInteractionActivityEncoded)
        val expected: ActivityDefinition = likertInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (long-fill-in)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](longFillInInteractionActivityEncoded)
        val expected: ActivityDefinition = longFillInInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (matching)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](matchingInteractionActivityEncoded)
        val expected: ActivityDefinition = matchingInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (numeric)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](numericInteractionActivityEncoded)
        val expected: ActivityDefinition = numericInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (other)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](otherInteractionActivityEncoded)
        val expected: ActivityDefinition = otherInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (performance)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](performanceInteractionActivityEncoded)
        val expected: ActivityDefinition = performanceInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (sequencing)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](sequencingInteractionActivityEncoded)
        val expected: ActivityDefinition = sequencingInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }

      it("should successfully decode a interaction activity definition (true-false)") {
        val decoded: Either[io.circe.Error, ActivityDefinition] =
          decode[ActivityDefinition](trueFalseInteractionActivityEncoded)
        val expected: ActivityDefinition = trueFalseInteractionActivity
        decoded match {
          case Right(actual) => assert(actual === expected)
          case Left(err)     => throw new Error(s"Decoding failed: $err")
        }
      }
    }
  }
}
