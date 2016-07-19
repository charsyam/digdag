package io.digdag.standards.operator;

import com.google.inject.Inject;
import io.digdag.spi.Operator;
import io.digdag.spi.OperatorFactory;
import io.digdag.spi.TaskExecutionContext;
import io.digdag.spi.TaskRequest;
import io.digdag.spi.TaskResult;
import io.digdag.util.BaseOperator;

import java.nio.file.Path;

public class FoobarOperatorFactory
        implements OperatorFactory
{
    @Inject
    public FoobarOperatorFactory()
    {
    }

    public String getType()
    {
        return "foobar";
    }

    @Override
    public Operator newTaskExecutor(Path workspacePath, TaskRequest request)
    {
        return new FoobarOperator(workspacePath, request);
    }

    private static class FoobarOperator
            extends BaseOperator
    {
        private FoobarOperator(Path workspacePath, TaskRequest request)
        {
            super(workspacePath, request);
        }

@Override
public TaskResult runTask(TaskExecutionContext ctx)
{
    String credential = ctx.secretProvider().getSecret("foobar_credential");

    // ... use the credential to e.g. communicate with some database etc

    return TaskResult.empty(request);
}
    }
}
