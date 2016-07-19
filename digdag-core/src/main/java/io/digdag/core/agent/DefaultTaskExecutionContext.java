package io.digdag.core.agent;

import io.digdag.spi.SecretProvider;
import io.digdag.spi.TaskExecutionContext;

public class DefaultTaskExecutionContext
        implements TaskExecutionContext
{
    @Override
    public SecretProvider secrets()
    {
        return null;
    }
}
