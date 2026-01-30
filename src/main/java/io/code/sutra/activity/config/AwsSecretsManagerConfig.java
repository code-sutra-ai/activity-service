package io.code.sutra.activity.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration
@ConditionalOnProperty(prefix = "app.secrets", name = "enabled", havingValue = "true")
public class AwsSecretsManagerConfig {
    private static final Logger log = LoggerFactory.getLogger(AwsSecretsManagerConfig.class);

    private final ConfigurableEnvironment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AwsSecretsManagerConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void loadSecrets() {
        String names = environment.getProperty("app.secrets.names", "");
        if (names == null || names.trim().isEmpty()) {
            log.info("No AWS secrets configured (app.secrets.names is empty). Skipping Secrets Manager fetch.");
            return;
        }

        boolean prodActive = Stream.of(environment.getActiveProfiles()).anyMatch(p -> "prod".equalsIgnoreCase(p));

        // Optionally accept region override via app.secrets.region
        String regionProp = environment.getProperty("app.secrets.region");

        try (SecretsManagerClient client = buildClient(regionProp)) {
            Map<String, Object> props = new HashMap<>();

            for (String rawName : Stream.of(names.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList())) {
                try {
                    log.info("Fetching secret '{}' from AWS Secrets Manager", rawName);
                    GetSecretValueRequest req = GetSecretValueRequest.builder().secretId(rawName).build();
                    GetSecretValueResponse resp = client.getSecretValue(req);
                    String secretString = resp.secretString();
                    if (secretString == null) {
                        log.warn("Secret '{}' returned no string value (binary?). Skipping.", rawName);
                        continue;
                    }

                    String safeName = sanitizeName(rawName);
                    // put the raw secret under secrets.<name>
                    props.put("secrets." + safeName, secretString);

                    // if JSON, attempt to parse and expose nested keys as secrets.<name>.<key>
                    Map<String, Object> flattened = new HashMap<>();
                    try {
                        var parsed = objectMapper.readValue(secretString, new TypeReference<Map<String, Object>>() {});
                        flattened = flattenMap(parsed);
                        flattened.forEach((k, v) -> props.put("secrets." + safeName + "." + k, String.valueOf(v)));
                    } catch (Exception e) {
                        // not JSON — that's fine
                    }

                    // Also, if the secret id itself is a special key like COGNITO_ACCOUNT_URL or similar,
                    // register it at top-level so application code can read it via @Value("${COGNITO_ACCOUNT_URL}")
                    if (isCognitoAccountUrlName(rawName)) {
                        props.put("COGNITO_ACCOUNT_URL", secretString);
                        props.put("cognito.account.url", secretString);
                    }

                    // Check flattened JSON keys for a COGNITO_ACCOUNT_URL entry and map it to top-level props as well
                    for (Map.Entry<String, Object> fe : flattened.entrySet()) {
                        String fk = fe.getKey();
                        if (fk == null) continue;
                        String candidate = fk.replaceAll("[^A-Za-z0-9]", "_").toUpperCase();
                        // Match explicit keys like COGNITO_ACCOUNT_URL or variations
                        if (candidate.equals("COGNITO_ACCOUNT_URL") || candidate.contains("COGNITO") && candidate.contains("ACCOUNT") && (candidate.contains("URL") || candidate.contains("URI"))) {
                            String val = fe.getValue() == null ? null : String.valueOf(fe.getValue());
                            if (val != null) {
                                props.put("COGNITO_ACCOUNT_URL", val);
                                props.put("cognito.account.url", val);
                            }
                        }
                    }

                    // Only map Cognito-specific keys into Spring properties when prod profile is active
                    if (prodActive) {
                        // Map common Cognito keys from flattened JSON into Spring properties
                        for (Map.Entry<String, Object> entry : flattened.entrySet()) {
                            String k = entry.getKey();
                            String val = entry.getValue() == null ? null : String.valueOf(entry.getValue());
                            if (val == null) continue;
                            String normalized = k.replaceAll("[^A-Za-z0-9]", "").toLowerCase();

                            switch (normalized) {
                                case "clientid":
                                    props.put("spring.security.oauth2.client.registration.cognito.client-id", val);
                                    break;
                                case "clientsecret":
                                    props.put("spring.security.oauth2.client.registration.cognito.client-secret", val);
                                    break;
                                case "redirecturi":
                                case "redirect":
                                    // registration redirect-uri expects a single URI string
                                    props.put("spring.security.oauth2.client.registration.cognito.redirect-uri", val);
                                    break;
                                case "scope":
                                    // scope may be a list/array; ensure it's a comma-separated string
                                    // If the value looks like [a, b], keep as-is; consumers can parse or we could join lists later
                                    props.put("spring.security.oauth2.client.registration.cognito.scope", val);
                                    break;
                                case "issueruri":
                                case "issuer":
                                    props.put("spring.security.oauth2.client.provider.cognito.issuer-uri", val);
                                    break;
                                case "username":
                                case "userattribute":
                                case "user_name_attribute":
                                    props.put("spring.security.oauth2.client.provider.cognito.user-name-attribute", val);
                                    break;
                                default:
                                    // do nothing for unknown keys
                                    break;
                            }
                        }

                        // Additionally, if the secret name contains 'cognito' and the secretString is non-JSON,
                        // attempt to map common single-value secrets (heuristic)
                        if (rawName.toLowerCase().contains("cognito") && flattened.isEmpty()) {
                            if (secretString.startsWith("http://") || secretString.startsWith("https://")) {
                                props.put("spring.security.oauth2.client.provider.cognito.issuer-uri", secretString);
                                props.put("cognito.account.url", secretString);
                            } else if (secretString.length() > 20) {
                                props.put("spring.security.oauth2.client.registration.cognito.client-secret", secretString);
                            }
                        }
                    }

                } catch (Exception e) {
                    log.warn("Failed to fetch secret '{}': {}", rawName, e.getMessage());
                }
            }

            if (!props.isEmpty()) {
                MapPropertySource ps = new MapPropertySource("aws-secrets-property-source", props);
                environment.getPropertySources().addFirst(ps);
                log.info("Registered {} secret properties into Environment under property source 'aws-secrets-property-source'", props.size());
            } else {
                log.info("No secret properties were registered from AWS Secrets Manager");
            }

        } catch (Exception e) {
            log.warn("Unable to create AWS Secrets Manager client: {}", e.getMessage());
        }
    }

    private SecretsManagerClient buildClient(String regionProp) {
        try {
            if (regionProp != null && !regionProp.isBlank()) {
                return SecretsManagerClient.builder().region(Region.of(regionProp)).build();
            }
            // Use default provider chain
            return SecretsManagerClient.create();
        } catch (Exception e) {
            // rethrow to be handled by caller
            throw e;
        }
    }

    private String sanitizeName(String raw) {
        return raw.replaceAll("[^A-Za-z0-9_.-]", "_");
    }

    private boolean isCognitoAccountUrlName(String name) {
        if (name == null) return false;
        // replace dots, slashes, whitespace, and hyphens with underscore for normalization
        String normalized = name.replaceAll("[./\\s-]", "_").toUpperCase();
        return normalized.equals("COGNITO_ACCOUNT_URL") || normalized.endsWith("COGNITO_ACCOUNT_URL") || normalized.equals("COGNITOURL") || normalized.contains("COGNITO") && normalized.contains("ACCOUNT");
    }

    private Map<String, Object> flattenMap(Map<String, Object> map) {
        Map<String, Object> out = new HashMap<>();
        flattenRecursive(map, "", out);
        return out;
    }

    private void flattenRecursive(Map<String, Object> map, String prefix, Map<String, Object> out) {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
            Object val = e.getValue();
            if (val instanceof Map) {
                flattenRecursive((Map<String, Object>) val, key, out);
            } else {
                out.put(key, val);
            }
        }
    }
}
