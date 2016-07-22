package io.digdag.core.database;

import com.google.inject.Inject;
import io.digdag.client.config.Config;
import io.digdag.spi.SecretStore;
import io.digdag.spi.SecretStoreProvider;

public class DatabaseSecretStoreManager
        implements SecretStoreProvider
{
    @Inject
    public DatabaseSecretStoreManager(Config systemConfig)
    {

    }

    @Override
    public SecretStore getSecretStore(int siteId)
    {
        return new DatabaseSecretStore(siteId);
    }
}
