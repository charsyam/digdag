package io.digdag.core.database;

import com.google.inject.Inject;
import io.digdag.client.config.Config;
import io.digdag.spi.SecretStore;
import io.digdag.spi.SecretControlStore;
import io.digdag.spi.SecretStoreProvider;
import org.skife.jdbi.v2.DBI;

public class DatabaseSecretStoreManager
        implements SecretStoreProvider
{
    private final Config systemConfig;
    private final DatabaseConfig config;
    private final DBI dbi;

    @Inject
    public DatabaseSecretStoreManager(Config systemConfig, DatabaseConfig config, DBI dbi)
    {

        this.systemConfig = systemConfig;
        this.config = config;
        this.dbi = dbi;
    }

    @Override
    public SecretStore getSecretStore(int siteId)
    {
        return new DatabaseSecretStore(config, dbi, siteId);
    }

    @Override
    public SecretControlStore getSecretStoreControl(int siteId)
    {
        return new DatabaseSecretControlStore(config, dbi, siteId);
    }
}
