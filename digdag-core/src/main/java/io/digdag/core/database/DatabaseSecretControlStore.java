package io.digdag.core.database;

import io.digdag.spi.SecretControlStore;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

class DatabaseSecretControlStore
        extends BasicDatabaseStoreManager<DatabaseSecretControlStore.Dao>
        implements SecretControlStore
{
    private final int siteId;

    DatabaseSecretControlStore(DatabaseConfig config, DBI dbi, int siteId)
    {
        super(config.getType(), Dao.class, dbi);
        this.siteId = siteId;
    }

    @Override
    public void setProjectSecret(int projectId, String key, String value)
    {
        // TODO: encrypt value
        transaction((handle, dao, ts) -> {
            dao.deleteProjectSecret(siteId, projectId, key);
            dao.insertProjectSecret(siteId, projectId, key, value);
            return null;
        });
    }

    @Override
    public void deleteProjectSecret(int projectId, String key)
    {
        transaction((handle, dao, ts) -> {
            dao.deleteProjectSecret(siteId, projectId, key);
            return null;
        });
    }

    interface Dao
    {
        @SqlUpdate("delete from secrets" +
                " where site_id = :siteId and project_id = :projectId and key = :key")
        int deleteProjectSecret(@Bind("siteId") int siteId, @Bind("projectId") int projectId, @Bind("key") String key);

        @SqlUpdate("insert into secrets" +
                " (site_id, project_id, key, value, updated_at)" +
                " values (:siteId, :projectId, :key, :value, now())")
        int insertProjectSecret(@Bind("siteId") int siteId, @Bind("projectId") int projectId, @Bind("key") String key, @Bind("value") String value);
    }
}
