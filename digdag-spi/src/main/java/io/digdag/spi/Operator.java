package io.digdag.spi;

import com.google.common.collect.ImmutableList;

import java.util.List;

public interface Operator
{
    TaskResult run(TaskExecutionContext ctx);

    default List<String> secretSelectors() {
        return ImmutableList.of();
    }
}
