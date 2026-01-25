package io.code.sutra.activity.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class StepsHttpClient {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    private ResponseEntity<String> latestResponse;
    private String basePath = "/";

    @Before
    public void before() {
        // base path can be overridden by Background steps
    }

    private String url(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        String p = path.startsWith("/") ? path : ("/" + path);
        return "http://localhost:" + port + (basePath.endsWith("/") ? basePath.substring(0, basePath.length()-1) : basePath) + p;
    }

    @Given("the API is available at {string}")
    public void the_api_is_available_at(String path) {
        this.basePath = path;
    }

    @Given("the hello endpoint is available")
    public void hello_endpoint_available() {
        // nothing to do - just a human friendly step
    }

    @When("I call GET {string}")
    public void i_call_get(String path) {
        latestResponse = restTemplate.getForEntity(url(path), String.class);
    }

    @Then("the response status should be {int}")
    public void response_status_should_be(Integer status) {
        assertThat(latestResponse.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the response body should contain {string}")
    public void response_body_should_contain(String text) {
        assertThat(latestResponse.getBody()).contains(text);
    }

    @Given("the following tasks exist:")
    public void the_following_tasks_exist(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = String.format("{ \"title\": \"%s\", \"description\": \"%s\", \"assignee\": \"%s\" }",
                    row.get("title"), row.get("description"), row.get("assignee"));
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            latestResponse = restTemplate.postForEntity(url("/tasks"), entity, String.class);
            assertThat(latestResponse.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        }
    }

    @Given("the following users exist:")
    public void the_following_users_exist(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = String.format("{ \"name\": \"%s\" }", row.get("name"));
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            latestResponse = restTemplate.postForEntity(url("/users"), entity, String.class);
            assertThat(latestResponse.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        }
    }

    @When("I DELETE {string}")
    public void i_delete(String path) {
        latestResponse = restTemplate.exchange(url(path), HttpMethod.DELETE, null, String.class);
    }

    @When("I POST {string} with the above table")
    public void i_post_with_above_table(String path, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        List<String> objects = rows.stream().map(row ->
                String.format("{ \"title\": \"%s\", \"description\": \"%s\", \"assignee\": \"%s\" }",
                        row.get("title"), row.get("description"), row.get("assignee"))
        ).collect(Collectors.toList());
        String arrayJson = "[" + String.join(",", objects) + "]";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(arrayJson, headers);
        latestResponse = restTemplate.postForEntity(url(path), entity, String.class);
    }

    @When("I POST {string} with body:")
    public void i_post_with_body(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        latestResponse = restTemplate.postForEntity(url(path), entity, String.class);
    }

    @Given("a task exists with title {string} and assignee {string}")
    public void a_task_exists_with_title_and_assignee(String title, String assignee) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = String.format("{ \"title\": \"%s\", \"description\": \"auto-created\", \"assignee\": \"%s\" }",
                title, assignee);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        latestResponse = restTemplate.postForEntity(url("/tasks"), entity, String.class);
        assertThat(latestResponse.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
    }

    @When("I PATCH {string} with JSON:")
    public void i_patch_with_json(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        latestResponse = restTemplate.exchange(url(path), HttpMethod.PATCH, entity, String.class);
    }

    @Then("the response should contain a JSON array with size {int}")
    public void response_should_contain_array_with_size(Integer size) throws Exception {
        assertThat(latestResponse.getBody()).isNotNull();
        JsonNode tree = objectMapper.readTree(latestResponse.getBody());
        assertThat(tree.isArray()).isTrue();
        assertThat(tree.size()).isEqualTo(size.intValue());
    }

    @Then("the response JSON should have {string} equal to {string}")
    public void response_json_should_have_field_equal(String field, String value) {
        assertThat(latestResponse.getBody()).contains(String.format("\"%s\": \"%s\"", field, value));
    }
}
