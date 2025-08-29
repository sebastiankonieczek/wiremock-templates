# Demo of [WireMock](https://wiremock.org/docs/overview/) response templating

## WireMock basics

- mock server to fake external api calls
- multiple ways of running
  - docker (Testcontainers)
  - standalone process (java -jar ...)
  - junit jupiter extension -> I use this ;)
  - spring-boot integration
  - standalone jar and create server programmatically

## Features

### in this demo
- stub through API
  - java/python/golang client
- response templating
### not part of demo
- stub through json-files
- webhooks/callbacks
- proxying
  - partial mocking
  - record and playback
- scenarios
  - mimic state

## Alternatives

- [MockServer](https://www.mock-server.com/#what-is-mockserver)
- [Restito](https://github.com/mkotsur/restito)
- maybe more but google results where not too helpful

## [Response Templating](https://wiremock.org/docs/response-templating/)

- handlebars templates
- default mode is local (per stub)

### Useful template Functions

[Full List](https://wiremock.org/docs/response-templating/)

- #assign
- val
- capitalize
- xPath
- jsonPath
- #parseJson
- toJson
- #formatJson
- jsonArrayAdd
- jsonMerge
- #each
