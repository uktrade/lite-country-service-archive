package uk.gov.bis.lite.countryservice;

import uk.gov.bis.lite.countryservice.resources.CountriesResource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CountryServiceApplicationTest {

    @Mock
    private Environment environment;

    @Mock
    private JerseyEnvironment jersey;

    private final CountryServiceApplication application = new CountryServiceApplication();
    private final CountryServiceConfiguration config = new CountryServiceConfiguration();

    @Before
    public void setup() throws Exception {
        config.setCacheExpirySeconds(10);
        when(environment.jersey()).thenReturn(jersey);
    }

    @Test
    public void buildsAThingResource() throws Exception {
        application.run(config, environment);

        verify(jersey).register(Mockito.isA(CountriesResource.class));
    }

}