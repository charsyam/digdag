package io.digdag.core.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import io.digdag.client.config.Config;
import io.digdag.spi.SecretAccessDeniedException;
import io.digdag.spi.SecretAccessPolicy;
import io.digdag.spi.SecretProvider;
import io.digdag.spi.SecretStore;

import java.util.ArrayList;
import java.util.List;

class DefaultSecretProvider
        implements SecretProvider
{
    private final String operatorType;
    private final SecretAccessPolicy secretAccessPolicy;
    private final Config grants;
    private final SecretFilter operatorSecretFilter;
    private final SecretStore secretStore;

    DefaultSecretProvider(
            String operatorType, SecretAccessPolicy secretAccessPolicy, Config grants, SecretFilter operatorSecretFilter, SecretStore secretStore)
    {
        this.operatorType = operatorType;
        this.secretAccessPolicy = secretAccessPolicy;
        this.grants = grants;
        this.operatorSecretFilter = operatorSecretFilter;
        this.secretStore = secretStore;
    }

    @Override
    public String getSecret(String key)
    {
        // Sanity check key
        String errorMessage = "Illegal key: '" + key + "'";
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), errorMessage);
        Preconditions.checkArgument(key.indexOf('*') == -1, errorMessage);
        List<String> segments = Splitter.on('.').splitToList(key);
        segments.forEach(segment -> Preconditions.checkArgument(!Strings.isNullOrEmpty(segment)));

        // Only allow operatorType to access pre-declared secrets
        if (!operatorSecretFilter.match(key)) {
            throw new SecretAccessDeniedException(key);
        }

        // If the key falls under the scope of an explicit grant, then fetch the secret identified by remounting the key path into the grant path.
        List<String> path = new ArrayList<>();
        JsonNode scope = grants.getInternalObjectNode();
        int i = 0;

        while (true) {
            String segment = segments.get(i);
            JsonNode node = scope.get(segment);
            if (node == null) {
                // Key falls outside the override scope. No override.
                break;
            }
            path.add(segment);
            if (node.isObject()) {
                // Dig deeper
                i++;
                if (i >= segments.size()) {
                    // Key ended before we reached a leaf. No override.
                    break;
                }
                scope = node;
            }
            else if (node.isTextual()) {
                // Reached a path-overriding grant leaf.
                List<String> remainder = segments.subList(i + 1, segments.size());
                List<String> base = Splitter.on('.').splitToList(node.asText());
                String remounted = FluentIterable
                        .from(base)
                        .append(remainder)
                        .join(Joiner.on('.'));
                return fetchSecret(remounted);
            } else if (node.isBoolean() && node.asBoolean()) {
                // Reached a grant leaf.
                return fetchSecret(key);
            }
            else {
                throw new AssertionError();
            }
        }

        // No explicit grant. Check key against system acl to see if access is granted by default.
        if (!secretAccessPolicy.isSecretAccessible(operatorType, key)) {
            throw new SecretAccessDeniedException(key);
        }

        return fetchSecret(key);
    }

    private String fetchSecret(String key)
    {
        return secretStore.getSecret(key);
    }
}
