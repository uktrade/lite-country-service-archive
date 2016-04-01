package prototype.countryservice.core.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import javax.xml.soap.SOAPMessage;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.dropwizard.testing.FixtureHelpers.fixture;

public class SpireGetCountriesClientTest {

    private static final String SOAP_URL = "http://localhost:9000/spirefox4dev/fox/ispire/SPIRE_COUNTRIES";
    private static final String SOAP_URI = "http://www.fivium.co.uk/fox/webservices/ispire/SPIRE_COUNTRIES/getCountries";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9000);

    private SpireGetCountriesClient spireGetCountriesClient = new SpireGetCountriesClient(SOAP_URL,
            SOAP_URI, SOAP_URI);

    @Test
    public void shouldExecuteSoapRequest() throws Exception {

        stubFor(post(urlEqualTo("/spirefox4dev/fox/ispire/SPIRE_COUNTRIES"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(fixture("spire-getCountries.xml"))));

        SOAPMessage soapMessage = spireGetCountriesClient.executeRequest("EXPORT_CONTROL");

        soapMessage.writeTo(System.out);
    }
}