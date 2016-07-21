package io.digdag.spi;

public interface SecretStoreProvider
{
    SecretStore getSecretStore(int siteId);
}
