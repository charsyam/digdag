package io.digdag.core.database;

import io.digdag.spi.SecretAccessDeniedException;
import io.digdag.spi.SecretAccessContext;
import io.digdag.spi.SecretStore;

class DatabaseSecretStore
        implements SecretStore
{
    private final int siteId;

    DatabaseSecretStore(int siteId)
    {
        this.siteId = siteId;
    }

    @Override
    public String getSecret(SecretAccessContext principal, String key)
    {
        if (principal.siteId() != siteId) {
            throw new SecretAccessDeniedException("Site id mismatch");
        }

        return null;
    }
}
