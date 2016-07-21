package io.digdag.cli;

import io.digdag.spi.SecretAccessPolicy;

class LocalSecretAccessPolicy
        implements SecretAccessPolicy
{
    @Override
    public boolean isSecretAccessible(String operator, String key)
    {
        // TODO: Restrict default access in local mode?
        return true;
    }
}
