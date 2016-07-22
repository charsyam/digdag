package io.digdag.cli;

import com.google.inject.Inject;
import io.digdag.client.config.Config;
import io.digdag.spi.SecretAccessContext;
import io.digdag.spi.SecretStore;
import io.digdag.spi.SecretStoreProvider;

import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

class LocalSecretStoreProvider
        implements SecretStoreProvider
{
    private final Map<String, String> secrets;

    @Inject
    public LocalSecretStoreProvider(Config systemConfig)
    {
        String prefix = "secrets.";
        this.secrets = systemConfig.getKeys().stream()
                .filter(k -> k.startsWith(prefix))
                .collect(toMap(
                        k -> k.substring(prefix.length(), k.length()),
                        k -> systemConfig.get(k, String.class)));
    }

    @Override
    public SecretStore getSecretStore(int siteId)
    {
        return new SecretStore()
        {
            @Override
            public String getSecret(SecretAccessContext context, String key)
            {
                return secrets.get(key);
            }
        };
    }
}
