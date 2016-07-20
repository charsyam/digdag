package io.digdag.spi;

public interface SecretAccessPolicy
{
    boolean isSecretAccessible(String operator, String key);
}
