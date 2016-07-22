package io.digdag.core.database;

import io.digdag.core.repository.ResourceNotFoundException;
import io.digdag.spi.SecretAccessDeniedException;
import io.digdag.spi.SecretAccessContext;
import io.digdag.spi.SecretStore;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

class DatabaseSecretStore
        extends BasicDatabaseStoreManager<DatabaseSecretStore.Dao>
        implements SecretStore
{
    private final int siteId;

    DatabaseSecretStore(DatabaseConfig config, DBI dbi, int siteId)
    {
        super(config.getType(), Dao.class, dbi);
        this.siteId = siteId;
    }

    @Override
    public String getSecret(SecretAccessContext context, String key)
    {
        if (context.siteId() != siteId) {
            throw new SecretAccessDeniedException("Site id mismatch");
        }

        try {
            return requiredResource(
                    (handle, dao) -> dao.getProjectSecret(siteId, context.projectId(), key),
                    "secret with key=%s in project id=%d", key, context.projectId());
        }
        catch (ResourceNotFoundException e) {
            return null;
        }
    }

    interface Dao
    {
        @SqlQuery("select value from secrets" +
                " where site_id = :siteId and project_id = :projectId and key = :key")
        String getProjectSecret(@Bind("siteId") int siteId, @Bind("projectId") int projectId, @Bind("key") String key);
    }
}
