package uk.gov.bis.lite.countryservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.countryservice.api.CountryData;
import uk.gov.bis.lite.countryservice.api.CountryView;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public class CountryDataIntegrationTest extends BaseIntegrationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(CountryDataIntegrationTest.class);
  private static final ObjectWriter WRITER = new ObjectMapper().writerWithDefaultPrettyPrinter();

  private static final String URL = "http://localhost:8090/country-data";

  @Test
  public void shouldReturnCountryData() throws JsonProcessingException {

    Response response = JerseyClientBuilder.createClient()
        .target(URL + "/CTRY3")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    CountryView countryView = response.readEntity(CountryView.class);

    assertThat(countryView.getCountryName()).isEqualTo("Abu Dhabi");
    assertThat(countryView.getCountryRef()).isEqualTo("CTRY3");
    assertThat(countryView.getSynonyms()).containsExactly("U.A.E.", "UAE", "United Arab Emirates");
  }

  @Test
  public void shouldReturn404ForGetCountryIfCountryRefDoesNotExist() {

    Response response = JerseyClientBuilder.createClient()
        .target(URL + "/MADE-UP")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(404);

    String expected = "{'code':404,'message':'The following countryRef does not exist: MADE-UP'}";
    String actual = response.readEntity(String.class);
    assertEquals(toJson(expected), actual, JSONCompareMode.NON_EXTENSIBLE);
  }

  @Test
  public void shouldUpdateCountryData() {

    CountryData updateCountryData = new CountryData(null, new String[]{"Emirates", "Dubai"});
    Response updateResponse = JerseyClientBuilder.createClient()
        .target(URL + "/CTRY3")
        .request()
        .put(Entity.json(updateCountryData));

    assertThat(updateResponse.getStatus()).isEqualTo(200);
    assertThat(updateResponse.hasEntity()).isEqualTo(false);

    // Make a get request to verify that the update is reflected in the server data
    CountryView countryViewAfterUpdate = getCountryView("CTRY3");

    assertThat(countryViewAfterUpdate.getCountryName()).isEqualTo("Abu Dhabi");
    assertThat(countryViewAfterUpdate.getCountryRef()).isEqualTo("CTRY3");
    assertThat(countryViewAfterUpdate.getSynonyms()).containsExactly("Dubai", "Emirates");
  }

  @Test
  public void shouldReturn404ForUpdateCountryDataIfCountryRefDoesNotExist() {

    CountryData updateCountryData = new CountryData(null, new String[]{"madeUp", "M.A.D.E.U.P."});
    Response response = JerseyClientBuilder.createClient()
        .target(URL + "/MADE-UP")
        .request()
        .put(Entity.json(updateCountryData));

    assertThat(response.getStatus()).isEqualTo(404);

    String expected = "{'code':404,'message':'The following countryRef does not exist: MADE-UP'}";
    String actual = response.readEntity(String.class);
    assertEquals(toJson(expected), actual, JSONCompareMode.NON_EXTENSIBLE);
  }

  @Test
  public void shouldDeleteCountryData() {

    // Verify that country has data before delete
    CountryView beforeDelete = getCountryView("CTRY3");
    assertThat(beforeDelete.getCountryName()).isEqualTo("Abu Dhabi");
    assertThat(beforeDelete.getCountryRef()).isEqualTo("CTRY3");
    assertThat(beforeDelete.getSynonyms()).containsExactly("U.A.E.", "UAE", "United Arab Emirates");

    // Delete country data, i.e. synonyms
    Response response = JerseyClientBuilder.createClient()
        .target(URL + "/CTRY3")
        .request()
        .delete();

    assertThat(response.getStatus()).isEqualTo(202);
    assertThat(response.hasEntity()).isFalse();

    // Verify that country has no more data after delete, i.e. no synonyms
    CountryView afterDelete = getCountryView("CTRY3");
    assertThat(afterDelete.getCountryName()).isEqualTo("Abu Dhabi");
    assertThat(afterDelete.getCountryRef()).isEqualTo("CTRY3");
    assertThat(afterDelete.getSynonyms()).isEmpty();
  }

  @Test
  public void shouldGetAllCountries() throws JsonProcessingException {
    Response response = JerseyClientBuilder.createClient()
        .target(URL)
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<CountryView> countryViews = response.readEntity(new GenericType<List<CountryView>>() {
    });

    assertThat(countryViews).hasSize(21);
    assertThat(countryViews).extracting(CountryView::getCountryName).containsExactly(
        "Abu Dhabi", "Afghanistan", "Ajman", "Aland Islands", "Albania", "Alderney", "Algeria", "All Countries",
        "American Samoa", "Andorra", "Angola", "Austria", "Belgium", "Bulgaria", "Croatia", "Cyprus", "Czech Republic",
        "Denmark", "Estonia", "Finland", "France");

    // Verify country with synonym
    CountryView france = countryViews.stream()
        .filter(countryView -> countryView.getCountryName().equals("France"))
        .findAny()
        .get();

    assertThat(france.getCountryRef()).isEqualTo("CTRY1434");
    assertThat(france.getSynonyms()).containsExactly("French Republic");

    // Verify country without synonyms
    CountryView algeria = countryViews.stream()
        .filter(countryView -> countryView.getCountryName().equals("Algeria"))
        .findAny()
        .get();

    assertThat(algeria.getCountryRef()).isEqualTo("CTRY293");
    assertThat(algeria.getSynonyms()).isEmpty();
  }

  @Test
  public void shouldReturn404ForBulkUpdateIfCountryRefNotFound() {

    CountryData madeUpCountryRef = new CountryData("MADE-UP", new String[]{});
    CountryData nullCountryRef = new CountryData(null, new String[]{});
    List<CountryData> countryDataList = Arrays.asList(madeUpCountryRef, nullCountryRef, null);

    Response response = JerseyClientBuilder.createClient()
        .target(URL)
        .request()
        .put(Entity.json(countryDataList));

    assertThat(response.getStatus()).isEqualTo(404);

    String expected = "{'code':404,'message':'The following countryRef do not exist: MADE-UP, null, null'}";
    String actual = response.readEntity(String.class);
    assertEquals(toJson(expected), actual, JSONCompareMode.NON_EXTENSIBLE);
  }

  @Test
  public void shouldReturn400ForBulkUpdateIfDuplicateRefFound() {
    CountryData austria = new CountryData("CTRY781", new String[]{});
    CountryData france = new CountryData("CTRY1434", new String[]{});
    List<CountryData> countryDataList = Arrays.asList(austria, france, austria, france, france);

    Response response = JerseyClientBuilder.createClient()
        .target(URL)
        .request()
        .put(Entity.json(countryDataList));

    assertThat(response.getStatus()).isEqualTo(400);

    assertThat(response.readEntity(String.class)).contains("The following countryRef are duplicate:", "CTRY1434", "CTRY781");
  }

  @Test
  public void shouldBulkUpdate() {

    CountryData abuDhabi = new CountryData("CTRY3", new String[]{"Arab Emirates", "Dubai"});
    CountryData france = new CountryData("CTRY1434", null);
    List<CountryData> countryDataList = Arrays.asList(abuDhabi, france);

    Response response = JerseyClientBuilder.createClient()
        .target(URL)
        .request()
        .put(Entity.json(countryDataList));

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.hasEntity()).isFalse();

    // Verify that update was successful
    CountryView abuDhabiAfterUpdate = getCountryView("CTRY3");
    assertThat(abuDhabiAfterUpdate.getCountryRef()).isEqualTo("CTRY3");
    assertThat(abuDhabiAfterUpdate.getCountryName()).isEqualTo("Abu Dhabi");
    assertThat(abuDhabiAfterUpdate.getSynonyms()).containsExactly("Arab Emirates", "Dubai");

    CountryView franceAfterUpdate = getCountryView("CTRY1434");
    assertThat(franceAfterUpdate.getCountryRef()).isEqualTo("CTRY1434");
    assertThat(franceAfterUpdate.getCountryName()).isEqualTo("France");
    assertThat(franceAfterUpdate.getSynonyms()).isEmpty();
  }

  @Test
  public void shouldDeleteAllCountries() {

    // Verify for a specific country that it has data before delete
    CountryView beforeDelete = getCountryView("CTRY3");
    assertThat(beforeDelete.getCountryName()).isEqualTo("Abu Dhabi");
    assertThat(beforeDelete.getCountryRef()).isEqualTo("CTRY3");
    assertThat(beforeDelete.getSynonyms()).containsExactly("U.A.E.", "UAE", "United Arab Emirates");

    // Make request to delete data, i.e. synonyms, for all countries
    Response response = JerseyClientBuilder.createClient()
        .target(URL)
        .request()
        .delete();

    assertThat(response.getStatus()).isEqualTo(202);
    assertThat(response.hasEntity()).isFalse();

    // Verify that the country has no more synonyms, but that countryName and countryRef remain intact
    CountryView afterDelete = getCountryView("CTRY3");
    assertThat(afterDelete.getCountryName()).isEqualTo("Abu Dhabi");
    assertThat(afterDelete.getCountryRef()).isEqualTo("CTRY3");
    assertThat(afterDelete.getSynonyms()).isEmpty();

    // Verify that all other countries have no synonyms either
    List<CountryView> countryViewsAfterDelete = JerseyClientBuilder.createClient()
        .target(URL)
        .request()
        .get()
        .readEntity(new GenericType<List<CountryView>>() {
        });

    assertThat(countryViewsAfterDelete).hasSize(21);
    assertThat(countryViewsAfterDelete).extracting(CountryView::getSynonyms).containsOnly(new String[]{});
  }

  private CountryView getCountryView(String countryRef) {
    return JerseyClientBuilder.createClient()
        .target(URL + "/" + countryRef)
        .request()
        .get()
        .readEntity(CountryView.class);
  }

  private String toJson(String str) {
    return str.replace("'", "\"");
  }

}
