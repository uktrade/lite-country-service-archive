package uk.gov.bis.lite.countryservice.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.bis.lite.countryservice.model.CountryEntry;
import uk.gov.bis.lite.countryservice.service.SpireService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CountryCacheTest {

  @Mock
  private SpireService spireService;

  private CountryCache countryCache;

  private final CountryEntry albania = new CountryEntry("1", "Albania");
  private final CountryEntry brazil = new CountryEntry("2", "Brazil");
  private final CountryEntry finland = new CountryEntry("3", "Finland");
  private final CountryEntry france = new CountryEntry("4", "France");
  private final CountryEntry germany = new CountryEntry("5", "Germany");
  private final CountryEntry sweden = new CountryEntry("6", "Sweden");

  @Before
  public void setUp() throws Exception {
    countryCache = new CountryCache(spireService);
    List<CountryEntry> countrySet = Arrays.asList(albania, brazil, finland);
    List<CountryEntry> countryGroup = Arrays.asList(france, germany, sweden);

    when(spireService.loadCountriesByCountrySetId("EXPORT_CONTROL")).thenReturn(countrySet);
    when(spireService.loadCountriesByCountryGroupId("EU")).thenReturn(countryGroup);

    countryCache.load();
  }

  @Test
  public void shouldGetCountrySetFromCache() {

    Optional<List<CountryEntry>> countryListEntry = countryCache.getCountriesBySetName("export-control");

    assertThat(countryListEntry.get()).containsExactly(albania, brazil, finland);
  }

  @Test
  public void shouldGetEmptyListWhenCountrySetNotFoundInCache() {

    Optional<List<CountryEntry>> countryListEntry = countryCache.getCountriesBySetName("blah");

    assertThat(countryListEntry).isEmpty();
  }

  @Test
  public void shouldGetCountryGroupFromCache() {

    Optional<List<CountryEntry>> countryListEntry = countryCache.getCountriesByGroupName("eu");

    assertThat(countryListEntry.get()).containsExactly(france, germany, sweden);
  }

  @Test
  public void shouldGetEmptyListWhenCountryGroupNotFoundInCache() {

    Optional<List<CountryEntry>> countryListEntry = countryCache.getCountriesByGroupName("blah");

    assertThat(countryListEntry).isEmpty();
  }

  @Test
  public void shouldGetCountryEntries() {
    Collection<CountryEntry> countryEntries = countryCache.getCountryEntries();
    assertThat(countryEntries).hasSize(6);
    assertThat(countryEntries).containsExactlyInAnyOrder(albania, brazil, finland, france, germany, sweden);
  }

  @Test
  public void shouldGetCountryEntry() {
    Optional<CountryEntry> countryEntry = countryCache.getCountryEntry("1");

    assertThat(countryEntry.get()).isEqualTo(albania);
  }

  @Test
  public void shouldGetEmptyOptionalForUnknownCountryRef() {
    Optional<CountryEntry> countryEntry = countryCache.getCountryEntry("MADE-UP");

    assertThat(countryEntry).isEmpty();
  }

  @Test
  public void shouldGetLastCached() {
    assertThat(countryCache.getLastCached()).isPositive();
  }

}
