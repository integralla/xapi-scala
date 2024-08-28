# xAPI Scala

[![Build Status](https://github.com/integralla/xapi-scala/actions/workflows/scala.yml/badge.svg)](https://github.com/integralla/xapi-scala/actions/workflows/scala.yml)

## Description

A Scala library for creating and working with Experience API (xAPI) resources (such as statements
and documents).

> The [Experience API (xAPI)](https://xapi.ieee-saopen.org/) is a standard that describes an
> interoperable means to document and communicate information about learning experiences. It
> specifies a structure to describe learning experiences and defines how these descriptions can be
> exchanged electronically.

## Encoding / Decoding

This library uses [circe](https://circe.github.io/circe/), for encoding / decoding statements and
other resources. Semi-automatic derivation is used in most cases, with custom codecs used for
complex scenarios.

## Statement Manipulation

On decoding statements, we've chosen to keep the modifications to the minimum required for
validation, deferring all other necessary manipulations to downstream processing such as would
typically occur before persistence.

The modifications that are performed on decoding are:

* When a property of context.contextActivities (`ContextActivities`) is a single activity object, it
  is converted into an array of activity objects having a single value

## Statement Validation

Where appropriate, object validation occurs on decoding a statement. Currently, validation is
limited to ensuring that the correct combinations of (optional) properties are present and that
properties that can / should be parsed and handled as a specific reference type (for example, an
IRI) can be parsed and handled as such.

If validation fails, a `StatementValidationException` will be thrown with an explanatory message
indicating why validation failed.

JSON schema validation is assumed for basic validation, including pattern matching against strings
for such things as UUIDs, hashes, etc.
