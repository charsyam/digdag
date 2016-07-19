package io.digdag.spi;

public interface SecretProvider
{
    String getSecret(String key);
}
