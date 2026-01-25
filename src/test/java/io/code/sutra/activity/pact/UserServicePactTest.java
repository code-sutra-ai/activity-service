package io.code.sutra.activity.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "UserServiceProvider", pactVersion = PactSpecVersion.V3)
public class UserServicePactTest {

    @Pact(consumer = "UserServiceConsumer")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        String reqBody = "{\"name\": \"alice\"}";
        String respBody = "{\"id\": 101, \"name\": \"alice\"}";

        return builder
                .given("provider accepts a new user")
                .uponReceiving("a request to create a user")
                .path("/api/users")
                .method("POST")
                .headers(Map.of("Content-Type", "application/json"))
                .body(reqBody)
                .willRespondWith()
                .status(201)
                .headers(Map.of("Content-Type", "application/json"))
                .body(respBody)
                .toPact();
    }

    @Test
    void runPact(MockServer mockServer) throws Exception {
        String url = mockServer.getUrl() + "/api/users";
        String payload = "{\"name\": \"alice\"}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(201);
        // optionally assert response body matches expected JSON structure
        assertThat(response.body()).contains("\"id\"");
        assertThat(response.body()).contains("\"name\": \"alice\"");
    }
}
