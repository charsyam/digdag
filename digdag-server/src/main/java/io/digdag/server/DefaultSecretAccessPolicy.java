package io.digdag.server;

import com.google.inject.Inject;
import io.digdag.client.config.Config;
import io.digdag.spi.SecretAccessContext;
import io.digdag.spi.SecretAccessPolicy;

public class DefaultSecretAccessPolicy
        implements SecretAccessPolicy
{
    @Inject
    public DefaultSecretAccessPolicy(Config systemConfig)
    {
        
    }

    @Override
    public boolean isSecretAccessible(SecretAccessContext context, String key)
    {
        return false;
    }
}
