package io.digdag.cli;

import io.digdag.spi.SecretAccessContext;
import io.digdag.spi.SecretStore;
import io.digdag.spi.SecretStoreProvider;

class LocalSecretStoreProvider
        implements SecretStoreProvider
{
    @Override
    public SecretStore getSecretStore(int siteId)
    {
        return new SecretStore() {
            @Override
            public String getSecret(SecretAccessContext context, String key)
            {
                return null;
            }
        };
    }
}
