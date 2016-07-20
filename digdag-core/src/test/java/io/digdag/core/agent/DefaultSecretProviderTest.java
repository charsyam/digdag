package io.digdag.core.agent;

import com.google.common.collect.ImmutableList;
import io.digdag.client.config.Config;
import io.digdag.spi.SecretAccessDeniedException;
import io.digdag.spi.SecretAccessPolicy;
import io.digdag.spi.SecretStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static io.digdag.core.database.DatabaseTestingUtils.createConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSecretProviderTest
{
    private static final String OPERATOR_TYPE = "test-operator";

    @Rule public final ExpectedException exception = ExpectedException.none();

    @Mock SecretAccessPolicy secretAccessPolicy;
    @Mock SecretStore secretStore;

    @Test
    public void verifyUndeclaredAccessIsDenied()
            throws Exception
    {
        verifyUndeclaredAccessIsDenied("foo", ImmutableList.of());
        verifyUndeclaredAccessIsDenied("foo", ImmutableList.of("bar"));
        verifyUndeclaredAccessIsDenied("foo", ImmutableList.of("bar.*"));
        verifyUndeclaredAccessIsDenied("foo", ImmutableList.of("foobar.*"));
    }

    private void verifyUndeclaredAccessIsDenied(String key, List<String> operatorSelectors)
    {
        Config grants = createConfig();
        DefaultSecretProvider provider = new DefaultSecretProvider(OPERATOR_TYPE, secretAccessPolicy, grants, operatorSelectors, secretStore);

        try {
            provider.getSecret(key);
            fail("Expected " + SecretAccessDeniedException.class.getName());
        }
        catch (SecretAccessDeniedException e) {
            assertThat(e.getKey(), is(key));
        }

        verifyNoMoreInteractions(secretStore);
        verifyNoMoreInteractions(secretAccessPolicy);
    }

    @Test
    public void testDefaultAccessibleSecret()
            throws Exception
    {
        String expectedSecret = "foo-secret";
        String key = "foo";

        when(secretStore.getSecret(key)).thenReturn(expectedSecret);

        when(secretAccessPolicy.isSecretAccessible(anyString(), anyString())).thenReturn(true);

        Config grants = createConfig();
        List<String> operatorSelectors = ImmutableList.of(key);
        DefaultSecretProvider provider = new DefaultSecretProvider(OPERATOR_TYPE, secretAccessPolicy, grants, operatorSelectors, secretStore);

        String secret = provider.getSecret(key);

        verify(secretAccessPolicy).isSecretAccessible(OPERATOR_TYPE, key);
        verify(secretStore).getSecret(key);

        assertThat(secret, is(expectedSecret));
    }

    @Test
    public void testGrantedSecret()
            throws Exception
    {
        String expectedSecret = "bar-secret";
        String key = "foo";
        String grantedKey = "bar";

        when(secretStore.getSecret(grantedKey)).thenReturn(expectedSecret);

        Config grants = createConfig();
        grants.set(key, grantedKey);

        List<String> operatorSelectors = ImmutableList.of(key);
        DefaultSecretProvider provider = new DefaultSecretProvider(OPERATOR_TYPE, secretAccessPolicy, grants, operatorSelectors, secretStore);

        String secret = provider.getSecret(key);

        verifyNoMoreInteractions(secretAccessPolicy);
        verify(secretStore).getSecret(grantedKey);

        assertThat(secret, is(expectedSecret));
    }

    @Test
    public void testSecretInGrantedSubTree()
            throws Exception
    {
        String expectedSecret = "bar-secret";
        String keyPrefix = "foo";
        String key = keyPrefix + ".secret";
        String grantedKeyPrefix = "bar";
        String grantedKey = grantedKeyPrefix + ".secret";

        when(secretStore.getSecret(grantedKey)).thenReturn(expectedSecret);

        Config grants = createConfig();
        grants.set(keyPrefix, grantedKeyPrefix);

        List<String> operatorSelectors = ImmutableList.of(key);
        DefaultSecretProvider provider = new DefaultSecretProvider(OPERATOR_TYPE, secretAccessPolicy, grants, operatorSelectors, secretStore);

        String secret = provider.getSecret(key);

        verifyNoMoreInteractions(secretAccessPolicy);
        verify(secretStore).getSecret(grantedKey);

        assertThat(secret, is(expectedSecret));
    }
}