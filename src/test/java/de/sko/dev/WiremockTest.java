package de.sko.dev;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@WireMockTest
class WiremockTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * In typical cases, we can just provide static responses for api calls.
   *
   * @param wireMockRuntimeInfo
   */
  @Test
  void basicWiremockTest(WireMockRuntimeInfo wireMockRuntimeInfo) {
    final var responseBody = new DTO(UUID.randomUUID().toString(), "responseFromServer");
    stubFor(
        get(urlPathEqualTo("/test")).willReturn(ResponseDefinitionBuilder.okForJson(responseBody)));

    try (HttpClient httpClient = HttpClient.newBuilder().build()) {
      final var response =
          httpClient.send(
              HttpRequest.newBuilder()
                  .GET()
                  .uri(URI.create(wireMockRuntimeInfo.getHttpBaseUrl() + "/test"))
                  .build(),
              HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(200);
      assertThatJson(response.body()).isEqualTo(MAPPER.writeValueAsString(responseBody));
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Assume we hava a Blackbox-Test (integration test) where we cannot determine the concrete api response of the request, as it is
   * dynamically derived from the request, that is also dynamically build within the business-logic.
   *
   * @param wireMockRuntimeInfo
   */
  @Test
  void responseTransformerTest(WireMockRuntimeInfo wireMockRuntimeInfo) {
    stubFor(
        post(urlPathEqualTo("/test"))
            .willReturn(
                aResponse()
                    .withStatus(202)
                    .withBody("{{request.body}}")
                    .withTransformers("response-template")));

    try (HttpClient httpClient = HttpClient.newBuilder().build()) {
      final var requestBody = new DTO(null, "responseFromServer");
      final var response =
          httpClient.send(
              HttpRequest.newBuilder()
                  .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(requestBody)))
                  .uri(URI.create(wireMockRuntimeInfo.getHttpBaseUrl() + "/test"))
                  .build(),
              HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(202);
      assertThatJson(response.body()).isEqualTo(json(requestBody));
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * What if we expect our api to extend the input somehow.
   *
   * @param wireMockRuntimeInfo
   */
  @Test
  void responseTransformerManipulationTest(WireMockRuntimeInfo wireMockRuntimeInfo) {
    stubFor(
        post(urlPathEqualTo("/test"))
            .willReturn(
                aResponse()
                    .withStatus(202)
                    .withBody("""
                              {{val request.body assign='currentBody'}}
                              {{#assign 'newId'}}
                              {\"id\":  1}
                              {{/assign}}
                              {{jsonMerge currentBody newId}}
                              """)
                    .withTransformers("response-template")));

    try (HttpClient httpClient = HttpClient.newBuilder().build()) {
      final var requestBody = new DTO(null, "responseFromServer");
      final var response =
          httpClient.send(
              HttpRequest.newBuilder()
                  .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(requestBody)))
                  .uri(URI.create(wireMockRuntimeInfo.getHttpBaseUrl() + "/test"))
                  .build(),
              HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(202);
      assertThatJson(response.body()).node("id").isEqualTo("1");
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void responseTransformerChangeFieldWithFormatTest(WireMockRuntimeInfo wireMockRuntimeInfo) {
    stubFor(
        post(urlPathEqualTo("/test"))
            .willReturn(
                aResponse()
                    .withStatus(202)
                    .withBody("""
                              {{val request.body assign='currentBody'}}
                              {{#assign 'newId'}}
                              {\"id\":  1, \"field\": \"response\"}
                              {{/assign}}
                              {{#assign 'extended'}}
                                {{jsonMerge currentBody newId}}
                              {{/assign}}
                              {{formatJson extended format='compact'}}
                              """)
                    .withTransformers("response-template")));

    try (HttpClient httpClient = HttpClient.newBuilder().build()) {
      final var requestBody = new DTO(null, "request");
      final var response =
          httpClient.send(
              HttpRequest.newBuilder()
                  .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(requestBody)))
                  .uri(URI.create(wireMockRuntimeInfo.getHttpBaseUrl() + "/test"))
                  .build(),
              HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(202);
      assertThatJson(response.body()).node("id").isEqualTo("1");
      assertThatJson(response.body()).node("field").isEqualTo("response");
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  record DTO(String id, String field) {}
}
