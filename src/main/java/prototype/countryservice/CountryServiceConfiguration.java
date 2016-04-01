package prototype.countryservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class CountryServiceConfiguration extends Configuration {

    @NotEmpty
    private String soapUrl;

    @NotEmpty
    private String soapAction;

    @NotEmpty
    private String soapNamespace;

    private Integer cacheExpiryMinutes;

    @JsonProperty
    public String getSoapUrl() {
        return soapUrl;
    }

    @JsonProperty
    public void setSoapUrl(String soapUrl) {
        this.soapUrl = soapUrl;
    }

    @JsonProperty
    public String getSoapAction() {
        return soapAction;
    }

    @JsonProperty
    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    @JsonProperty
    public String getSoapNamespace() {
        return soapNamespace;
    }

    @JsonProperty
    public void setSoapNamespace(String soapNamespace) {
        this.soapNamespace = soapNamespace;
    }

    @JsonProperty
    public Integer getCacheExpiryMinutes() {
        return cacheExpiryMinutes;
    }

    @JsonProperty
    public void setCacheExpiryMinutes(Integer cacheExpiryMinutes) {
        this.cacheExpiryMinutes = cacheExpiryMinutes;
    }
}
