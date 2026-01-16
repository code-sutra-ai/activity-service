package io.code.sutra.activity.automation.stepdefs;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SmokeSteps {

    private List<Map<String, String>> users;
    private final List<Map<String, Object>> processed = new ArrayList<>();

    @Given("the following users:")
    public void the_following_users(DataTable table) {
        users = table.asMaps(String.class, String.class);
    }

    @When("I validate the users")
    public void i_validate_the_users() {
        for (Map<String, String> u : users) {
            Map<String, Object> p = new HashMap<>();
            String name = u.get("name");
            String email = u.get("email");
            boolean active = Boolean.parseBoolean(u.getOrDefault("active", "false"));

            // simple validation example
            Assertions.assertNotNull(name, "name must not be null");
            Assertions.assertTrue(email != null && email.contains("@"), "email must contain @");

            p.put("name", name);
            p.put("email", email);
            p.put("active", active);
            processed.add(p);
        }
    }

    @Then("all users processed")
    public void all_users_processed() {
        Assertions.assertEquals(users.size(), processed.size(), "all rows should be processed");
    }
}

