package uk.gov.bis.lite.countryservice.spire.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "COUNTRY_LIST")
public class CountryList {

  @XmlElement(name = "COUNTRY")
  private final List<SpireCountry> countries;

  @SuppressWarnings("unused")
  private CountryList() {
    this(null);
  }

  public CountryList(List<SpireCountry> countries) {
    this.countries = countries;
  }

  public List<SpireCountry> getCountries() {
    return countries;
  }
}
