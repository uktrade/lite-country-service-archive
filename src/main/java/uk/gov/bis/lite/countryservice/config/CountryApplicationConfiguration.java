package uk.gov.bis.lite.countryservice.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.bis.lite.common.paas.db.SchemaAwareDataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CountryApplicationConfiguration extends Configuration {

  @NotNull
  @JsonProperty
  private Integer cacheExpirySeconds;

  @NotEmpty
  @JsonProperty
  private String countryCacheJobCron;

  @NotEmpty
  @JsonProperty
  private String spireClientUserName;

  @NotEmpty
  @JsonProperty
  private String spireClientPassword;

  @NotEmpty
  @JsonProperty
  private String spireClientUrl;

  @NotEmpty
  @JsonProperty
  private String adminLogin;

  @NotEmpty
  @JsonProperty
  private String adminPassword;

  @NotEmpty
  @JsonProperty
  private String serviceLogin;

  @NotEmpty
  @JsonProperty
  private String servicePassword;

  @Valid
  @NotNull
  @JsonProperty("database")
  private SchemaAwareDataSourceFactory dataSourceFactory;

  public Integer getCacheExpirySeconds() {
    return cacheExpirySeconds;
  }

  public String getCountryCacheJobCron() {
    return countryCacheJobCron;
  }

  public String getSpireClientUserName() {
    return spireClientUserName;
  }

  public String getSpireClientPassword() {
    return spireClientPassword;
  }

  public String getSpireClientUrl() {
    return spireClientUrl;
  }

  public DataSourceFactory getDataSourceFactory() {
    return dataSourceFactory;
  }

  public String getAdminLogin() {
    return adminLogin;
  }

  public String getAdminPassword() {
    return adminPassword;
  }

  public String getServiceLogin() {
    return serviceLogin;
  }

  public String getServicePassword() {
    return servicePassword;
  }

}
