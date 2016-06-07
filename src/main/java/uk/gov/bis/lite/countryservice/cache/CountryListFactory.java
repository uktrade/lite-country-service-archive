package uk.gov.bis.lite.countryservice.cache;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.countryservice.exception.CountryServiceException;
import uk.gov.bis.lite.countryservice.model.Country;
import uk.gov.bis.lite.countryservice.model.CountryList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.Collections;
import java.util.List;

public class CountryListFactory {

  private final JAXBContext jaxbContext;

  public CountryListFactory() throws JAXBException {
    this.jaxbContext = JAXBContext.newInstance(CountryList.class);
  }

  public List<Country> create(SOAPMessage soapResponse) {

    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      NodeList getCountriesResponse = soapResponse.getSOAPBody().getElementsByTagName("*");
      if (getCountriesResponse.getLength() > 0) {
        Node node = ((Element) getCountriesResponse.item(0)).getElementsByTagName("COUNTRY_LIST").item(0);
        CountryList countryList = unmarshaller.unmarshal(node, CountryList.class).getValue();
        return countryList.getCountries();
      }
      return Collections.emptyList();

    } catch (JAXBException | SOAPException e) {
      throw new CountryServiceException("Failed to create CountryList.", e);
    }

  }
}
