package io.digdag.cli;

import io.digdag.spi.SecretStore;

class LocalSecretStore implements SecretStore
{
    @Override
    public String getSecret(String key)
    {
        return null;
    }
}
