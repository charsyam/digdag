package io.digdag.core.agent;

import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigFactory;
import io.digdag.core.config.YamlConfigLoader;
import io.digdag.spi.SecretAccessDeniedException;
import io.digdag.spi.SecretAccessPolicy;
import io.digdag.spi.SecretStore;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.digdag.core.database.DatabaseTestingUtils.createConfig;
import static io.digdag.core.database.DatabaseTestingUtils.createConfigFactory;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class DefaultSecretProviderTest
{
    private static final String OPERATOR_TYPE = "test-operator";

    private static final YamlConfigLoader YAML_CONFIG_LOADER = new YamlConfigLoader();
    private static final ConfigFactory CONFIG_FACTORY = createConfigFactory();

    @Rule public final ExpectedException exception = ExpectedException.none();

    @Mock SecretAccessPolicy secretAccessPolicy;
    @Mock SecretStore secretStore;
    @Mock SecretFilter secretFilter;

    @Before
    public void setUp()
            throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void verifyUndeclaredAccessIsDenied()
            throws Exception
    {
        String key = "foo";
        Config grants = createConfig();

        when(secretFilter.match(anyString())).thenReturn(false);

        DefaultSecretProvider provider = new DefaultSecretProvider(OPERATOR_TYPE, secretAccessPolicy, grants, secretFilter, secretStore);

        try {
            provider.getSecret(key);
            fail("Expected " + SecretAccessDeniedException.class.getName());
        }
        catch (SecretAccessDeniedException e) {
            assertThat(e.getKey(), is(key));
        }

        verify(secretFilter).match(key);

        verifyNoMoreInteractions(secretStore);
        verifyNoMoreInteractions(secretAccessPolicy);
    }

    @Test
    public void testDefaultAccessibleSecret()
            throws Exception
    {
        String expectedSecret = "foo-secret";
        String key = "foo";
        Config grants = createConfig();

        when(secretStore.getSecret(key)).thenReturn(expectedSecret);
        when(secretAccessPolicy.isSecretAccessible(anyString(), anyString())).thenReturn(true);
        when(secretFilter.match(key)).thenReturn(true);

        DefaultSecretProvider provider = new DefaultSecretProvider(OPERATOR_TYPE, secretAccessPolicy, grants, secretFilter, secretStore);

        String secret = provider.getSecret(key);

        verify(secretFilter).match(key);
        verify(secretAccessPolicy).isSecretAccessible(OPERATOR_TYPE, key);
        verify(secretStore).getSecret(key);

        assertThat(secret, is(expectedSecret));
    }

    @Test
    @Parameters({
            "foo        | foo: true                  | foo",
            "foo        | foo: bar                   | bar",
            "foo.secret | foo: true                  | foo.secret",
            "foo.secret | foo: bar                   | bar.secret",
            "foo.a.b    | foo: {a: true}             | foo.a.b",
            "foo.a.b    | foo: {a: bar.a\\, b: quux} | bar.a.b"
    })
    public void testGrantedSecret(String key, String grantsYaml, String expectedKey)
            throws Exception
    {
        String expectedSecret = "the-secret";

        when(secretStore.getSecret(expectedKey)).thenReturn(expectedSecret);
        when(secretAccessPolicy.isSecretAccessible(anyString(), anyString())).thenReturn(false);
        when(secretFilter.match(key)).thenReturn(true);

        Config grants = YAML_CONFIG_LOADER.loadString(grantsYaml).toConfig(CONFIG_FACTORY);

        DefaultSecretProvider provider = new DefaultSecretProvider(OPERATOR_TYPE, secretAccessPolicy, grants, secretFilter, secretStore);

        String secret = provider.getSecret(key);

        verify(secretFilter).match(key);
        verifyNoMoreInteractions(secretAccessPolicy);
        verify(secretStore).getSecret(expectedKey);

        assertThat(secret, is(expectedSecret));
    }
}