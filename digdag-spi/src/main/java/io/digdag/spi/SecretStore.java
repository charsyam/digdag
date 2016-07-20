package io.digdag.spi;

public interface SecretStore
{
    String getSecret(String key);
}
