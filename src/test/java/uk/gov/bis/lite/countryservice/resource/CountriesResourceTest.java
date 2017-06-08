package uk.gov.bis.lite.countryservice.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.countryservice.api.CountryView;
import uk.gov.bis.lite.countryservice.cache.CountryListEntry;
import uk.gov.bis.lite.countryservice.exception.CountryServiceException;
import uk.gov.bis.lite.countryservice.exception.CountryServiceException.ServiceExceptionMapper;
import uk.gov.bis.lite.countryservice.service.CountriesService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

public class CountriesResourceTest {

  private CountriesService countriesService = mock(CountriesService.class);

  @Rule
  public final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CountriesResource(countriesService, 1000))
      .addProvider(ServiceExceptionMapper.class)
      .build();

  @Test
  public void shouldGetCountrySetResource() throws Exception {

    List<CountryView> countries = Arrays.asList(new CountryView("CTRY1", "France"), new CountryView("CTRY2", "Spain"));
    when(countriesService.getCountrySet("export-control")).thenReturn(Optional.of(new CountryListEntry(countries)));

    Response result = resources.client()
        .target("/countries/set/export-control")
        .request()
        .get();

    assertThat(result.getStatus()).isEqualTo(200);

    String expectedJson = "[{\"countryRef\":\"CTRY1\",\"countryName\":\"France\"}," +
        "{\"countryRef\":\"CTRY2\",\"countryName\":\"Spain\"}]";
    assertEquals(expectedJson,
        result.readEntity(String.class), false);
  }

  @Test
  public void shouldGetCountryGroupResource() throws Exception {

    List<CountryView> countries = Arrays.asList(new CountryView("CTRY1", "France"), new CountryView("CTRY2", "Spain"));
    when(countriesService.getCountryGroup("eu")).thenReturn(Optional.of(new CountryListEntry(countries)));

    Response result = resources.client()
      .target("/countries/group/eu")
      .request()
      .get();

    assertThat(result.getStatus()).isEqualTo(200);

    String expectedJson = "[{\"countryRef\":\"CTRY1\",\"countryName\":\"France\"}," +
      "{\"countryRef\":\"CTRY2\",\"countryName\":\"Spain\"}]";
    assertEquals(expectedJson,
      result.readEntity(String.class), false);
  }

  @Test
  public void shouldReturn404StatusCodeWhenCountrySetNotFound() throws Exception {

    when(countriesService.getCountrySet("blah")).thenReturn(Optional.empty());

    Response result = resources.client()
        .target("/countries/set/blah")
        .request()
        .get();

    assertThat(result.getStatus()).isEqualTo(404);
    assertThat(result.readEntity(String.class)).isEqualTo("{\"code\":404,\"message\":\"Country set does not exist - blah\"}");
  }

  @Test
  public void shouldReturn404StatusCodeWhenCountryGroupNotFound() throws Exception {

    when(countriesService.getCountryGroup("blah")).thenReturn(Optional.empty());

    Response result = resources.client()
        .target("/countries/group/blah")
        .request()
        .get();

    assertThat(result.getStatus()).isEqualTo(404);
    assertThat(result.readEntity(String.class)).isEqualTo("{\"code\":404,\"message\":\"Country group does not exist - blah\"}");
  }

  @Test
  public void shouldReturn500StatusCodeForServiceException() throws Exception {

    when(countriesService.getCountrySet("blah")).thenThrow(new CountryServiceException("service error", null));

    Response result = resources.client()
        .target("/countries/set/blah")
        .request()
        .get();

    assertThat(result.getStatus()).isEqualTo(500);

    assertThat(result.readEntity(String.class)).isEqualTo("{\"code\":500,\"message\":\"service error\"}");
  }

  @Test
  public void shouldFilterCountriesWithNegativeId() throws Exception {

    List<CountryView> countries = Arrays.asList(new CountryView("CTRY1", "France"), new CountryView("CTRY2", "Spain"),
        new CountryView(CountriesResource.NEGATIVE_COUNTRY_ID_PREFIX + "1", "Negative"));
    when(countriesService.getCountrySet("export-control")).thenReturn(Optional.of(new CountryListEntry(countries)));

    Response result = resources.client()
        .target("/countries/set/export-control")
        .request()
        .get();

    assertThat(result.getStatus()).isEqualTo(200);

    String expectedJson = "[{\"countryRef\":\"CTRY1\",\"countryName\":\"France\"}," +
        "{\"countryRef\":\"CTRY2\",\"countryName\":\"Spain\"}]";
    assertEquals(expectedJson, result.readEntity(String.class), false);
  }

}