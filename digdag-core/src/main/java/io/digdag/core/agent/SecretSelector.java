package io.digdag.core.agent;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.regex.Pattern;

class SecretSelector
{
    private final Pattern VALID_PATTERN = Pattern.compile("^(\\w+\\.)*\\w+(\\.\\*)?$");

    private final String pattern;

    private SecretSelector(String pattern)
    {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(pattern), "pattern");
        Preconditions.checkArgument(VALID_PATTERN.matcher(pattern).matches(), "pattern");

        this.pattern = pattern;
    }

    boolean match(String key)
    {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "key");
        if (pattern.endsWith(".*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return key.startsWith(prefix);
        }
        else {
            return pattern.equals(key);
        }
    }

    static SecretSelector of(String pattern)
    {
        return new SecretSelector(pattern);
    }
}
