package io.digdag.core.database;

import io.digdag.spi.SecretStore;
import io.digdag.spi.SecretStoreProvider;

public class DatabaseSecretStoreManager
        implements SecretStoreProvider
{
    @Override
    public SecretStore getSecretStore(int siteId)
    {
        return new DatabaseSecretStore(siteId);
    }
}
