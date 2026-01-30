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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
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

    private static final String DEFAULT_SECRET_NAME = "code-sutra-secrets";

    // precompiled patterns for normalization
    private static final Pattern NON_ALPHANUM = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern NON_ALPHANUM_UNDERSCORE = Pattern.compile("[^A-Za-z0-9]");

    // mapping of normalized keys -> Spring property to set (for prod mapping)
    private static final Map<String, String> COGNITO_KEY_TO_PROP = Map.ofEntries(
            Map.entry("clientid", "spring.security.oauth2.client.registration.cognito.client-id"),
            Map.entry("clientsecret", "spring.security.oauth2.client.registration.cognito.client-secret"),
            Map.entry("redirecturi", "spring.security.oauth2.client.registration.cognito.redirect-uri"),
            Map.entry("redirect", "spring.security.oauth2.client.registration.cognito.redirect-uri"),
            Map.entry("scope", "spring.security.oauth2.client.registration.cognito.scope"),
            Map.entry("issueruri", "spring.security.oauth2.client.provider.cognito.issuer-uri"),
            Map.entry("issuer", "spring.security.oauth2.client.provider.cognito.issuer-uri"),
            Map.entry("username", "spring.security.oauth2.client.provider.cognito.user-name-attribute"),
            Map.entry("userattribute", "spring.security.oauth2.client.provider.cognito.user-name-attribute"),
            Map.entry("user_name_attribute", "spring.security.oauth2.client.provider.cognito.user-name-attribute")
    );

    private final ConfigurableEnvironment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AwsSecretsManagerConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void loadSecrets() {
        String names = environment.getProperty("app.secrets.names", "");
        if (names == null || names.trim().isEmpty()) {
            names = DEFAULT_SECRET_NAME;
            log.info("app.secrets.names not set; defaulting to '{}'", names);
        }

        boolean prodActive = Stream.of(environment.getActiveProfiles()).anyMatch(p -> "prod".equalsIgnoreCase(p));

        String regionProp = environment.getProperty("app.secrets.region");

        try (SecretsManagerClient client = buildClient(regionProp)) {
            Map<String, Object> props = new HashMap<>();

            List<String> secretNames = Stream.of(names.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            for (String rawName : secretNames) {
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
                    props.put("secrets." + safeName, secretString);

                    Map<String, Object> flattened = new HashMap<>();
                    try {
                        var parsed = objectMapper.readValue(secretString, new TypeReference<Map<String, Object>>() {});
                        flattened = flattenMap(parsed);
                    } catch (Exception e) {
                        // not JSON; that's fine
                    }

                    mapFlattenedToProperties(flattened, safeName, rawName, props, prodActive);

                    if (isCognitoAccountUrlName(rawName)) {
                        props.put("COGNITO_ACCOUNT_URL", secretString);
                        props.put("cognito.account.url", secretString);
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

    private void mapFlattenedToProperties(Map<String, Object> flattened, String safeName, String rawName, Map<String, Object> props, boolean prodActive) {
        if (flattened == null || flattened.isEmpty()) return;

        for (Map.Entry<String, Object> entry : flattened.entrySet()) {
            String key = entry.getKey();
            Object rawVal = entry.getValue();
            if (key == null || rawVal == null) continue;

            // stringify value (collections/arrays -> comma separated)
            String value = stringifyValue(rawVal);

            props.put("secrets." + safeName + "." + key, value);

            String candidate = normalizeForComparison(key);
            if ((candidate.contains("COGNITO") && candidate.contains("ACCOUNT") && (candidate.contains("URL") || candidate.contains("URI"))) || candidate.equals("COGNITO_ACCOUNT_URL")) {
                props.put("COGNITO_ACCOUNT_URL", value);
                props.put("cognito.account.url", value);
            }

            if (prodActive) {
                String normalized = normalizeKeyForSwitch(key);
                String mappedProp = COGNITO_KEY_TO_PROP.get(normalized);
                if (mappedProp != null) {
                    props.put(mappedProp, value);
                }
            }
        }

        // Heuristic fallback if secret name indicates cognito and issuer wasn't set
        if (prodActive && rawNameContainsCognito(safeName) && !props.containsKey("spring.security.oauth2.client.provider.cognito.issuer-uri")) {
            String raw = (String) props.get("secrets." + safeName);
            if (raw != null) {
                if (raw.startsWith("http://") || raw.startsWith("https://")) {
                    props.put("spring.security.oauth2.client.provider.cognito.issuer-uri", raw);
                    props.put("cognito.account.url", raw);
                } else if (raw.length() > 20) {
                    props.put("spring.security.oauth2.client.registration.cognito.client-secret", raw);
                }
            }
        }
    }

    private String stringifyValue(Object rawVal) {
        if (rawVal instanceof Collection) {
            return ((Collection<?>) rawVal).stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
        }
        if (rawVal != null && rawVal.getClass().isArray()) {
            return Arrays.stream((Object[]) rawVal).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
        }
        return String.valueOf(rawVal);
    }

    private String normalizeKeyForSwitch(String k) {
        return NON_ALPHANUM.matcher(k == null ? "" : k).replaceAll("").toLowerCase();
    }

    private String normalizeForComparison(String k) {
        return NON_ALPHANUM_UNDERSCORE.matcher(k == null ? "" : k).replaceAll("_").toUpperCase();
    }

    private boolean rawNameContainsCognito(String rawName) {
        return rawName != null && rawName.toLowerCase().contains("cognito");
    }

    private SecretsManagerClient buildClient(String regionProp) {
        if (regionProp != null && !regionProp.isBlank()) {
            return SecretsManagerClient.builder().region(Region.of(regionProp)).build();
        }
        return SecretsManagerClient.create();
    }

    private String sanitizeName(String raw) {
        return raw == null ? "" : raw.replaceAll("[^A-Za-z0-9_.-]", "_");
    }

    private boolean isCognitoAccountUrlName(String name) {
        if (name == null) return false;
        String normalized = name.replaceAll("[./\\s-]", "_").toUpperCase();
        return normalized.equals("COGNITO_ACCOUNT_URL") || normalized.endsWith("COGNITO_ACCOUNT_URL") || normalized.equals("COGNITOURL") || (normalized.contains("COGNITO") && normalized.contains("ACCOUNT"));
    }

    private Map<String, Object> flattenMap(Map<String, Object> map) {
        Map<String, Object> out = new HashMap<>();
        flattenRecursive(map, "", out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private void flattenRecursive(Map<String, Object> map, String prefix, Map<String, Object> out) {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
            Object val = e.getValue();
            if (val instanceof Map) {
                flattenRecursive((Map<String, Object>) val, key, out);
            } else if (val instanceof Collection) {
                String joined = ((Collection<?>) val).stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
                out.put(key, joined);
            } else if (val != null && val.getClass().isArray()) {
                String joined = Arrays.stream((Object[]) val).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
                out.put(key, joined);
            } else {
                out.put(key, val);
            }
        }
    }
}
