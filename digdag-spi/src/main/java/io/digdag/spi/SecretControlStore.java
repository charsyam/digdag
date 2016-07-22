package io.digdag.spi;

public interface SecretControlStore
{
    void setProjectSecret(int projectId, String key, String value);

    void deleteProjectSecret(int projectId, String key);
}
