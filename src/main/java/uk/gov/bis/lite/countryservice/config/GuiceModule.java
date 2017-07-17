package uk.gov.bis.lite.countryservice.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import uk.gov.bis.lite.common.spire.client.SpireClientConfig;
import uk.gov.bis.lite.common.spire.client.SpireRequestConfig;
import uk.gov.bis.lite.countryservice.service.CountriesService;
import uk.gov.bis.lite.countryservice.service.CountriesServiceImpl;
import uk.gov.bis.lite.countryservice.spire.CountryParser;
import uk.gov.bis.lite.countryservice.spire.SpireCountriesClient;

import javax.inject.Named;
import javax.xml.bind.JAXBException;

public class GuiceModule extends AbstractModule implements ConfigurationAwareModule<CountryApplicationConfiguration> {

  private CountryApplicationConfiguration configuration;

  @Override
  protected void configure() {
    bind(CountriesService.class).to(CountriesServiceImpl.class);
  }

  @Override
  public void setConfiguration(CountryApplicationConfiguration configuration) {
    this.configuration = configuration;
  }

  @Provides
  @Singleton
  SpireCountriesClient provideCountryClient(Environment env, CountryApplicationConfiguration config) throws JAXBException {
    return new SpireCountriesClient(new CountryParser(),
        new SpireClientConfig(config.getSpireClientUserName(), config.getSpireClientPassword(), config.getSpireClientUrl()),
        new SpireRequestConfig("SPIRE_COUNTRIES", "getCountries", true));
  }

  @Provides
  @Singleton
  public DBI provideDataSourceDbi(Environment environment, CountryApplicationConfiguration configuration) {
    return new DBIFactory().build(environment, configuration.getDataSourceFactory(), "sqlite");
  }

  @Provides
  @Named("cacheExpirySeconds")
  int provideCacheExpirySeconds(CountryApplicationConfiguration configuration) {
    return configuration.getCacheExpirySeconds();
  }
}