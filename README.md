# LRS Model

## Description
A data model for [Experience API (xAPI) Statements](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#statement-properties)

## Encoding / Decoding
[circe](https://circe.github.io/circe/)  (_"A JSON library for Scala powered by Cats"_) is used for encoding / decoding 
statements. Semi-automatic derivation is used in most cases, with custom codecs used for complex scenarios.

## Statement Manipulation
On decoding statements, we've chosen to keep the modifications to the minimum required for validation, deferring all 
other necessary manipulations to downstream processing such as much occur before persistence. 

The modifications that are performed on decoding are:

* When a property of context.contextActivities (`ContextActivities`) is a single activity object, it is converted into 
an array of activity objects having a single value

## Statement Validation
Where appropriate, object validation occurs on decoding a statement. Currently, validation is limited to ensuring that 
the correct combinations of (optional) properties are present and that properties that can / should be parsed and 
handled as a specific reference type (for example, an IRI) can be parsed and handled as such.

If validation fails, a `StatementValidationException` will be thrown with an explanatory message indicating why 
validation failed.

JSON schema validation is assumed for basic validation, including pattern matching against strings for such things as 
UUIDs, hashes, etc.