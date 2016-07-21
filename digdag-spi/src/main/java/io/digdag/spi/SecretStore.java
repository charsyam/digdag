package io.digdag.spi;

public interface SecretStore
{
    String getSecret(SecretAccessContext principal, String key);
}
