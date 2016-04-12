package uk.gov.bis.lite.countryservice.core.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.countryservice.api.Country;
import uk.gov.bis.lite.countryservice.core.exception.CountryServiceException;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GetCountriesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCountriesService.class);

    private final SpireGetCountriesClient spireGetCountriesClient;
    private final CountryListFactory countryListFactory;
    private final LoadingCache<String, List<Country>> cache;
    private final ListeningExecutorService executorService;

    @Inject
    public GetCountriesService(SpireGetCountriesClient spireGetCountriesClient, CountryListFactory countryListFactory,
                               @Named("cacheExpirySeconds") Integer cacheExpirySeconds) throws JAXBException {
        this.spireGetCountriesClient = spireGetCountriesClient;
        this.countryListFactory = countryListFactory;
        this.executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10)); // hardcoded for now
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .refreshAfterWrite(cacheExpirySeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<Country>>() {
                    @Override
                    public List<Country> load(String key) throws Exception {
                        return loadCountries(key);
                    }

                    @Override
                    public ListenableFuture<List<Country>> reload(final String key, List<Country> oldValue) throws Exception {
                        return executorService.submit(() -> {
                            try {
                                return loadCountries(key);
                            } catch (CountryServiceException e) {
                                LOGGER.error("Returning stale value - {}", oldValue);
                                return oldValue;
                            }
                        });
                    }
                });
    }

    public List<Country> getCountryList(String countrySetName) throws CountryServiceException {
        try {
            return cache.get(countrySetName);
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage());
            throw new CountryServiceException(e);
        }
    }

    private List<Country> loadCountries(String countrySetName) throws CountryServiceException {
        try {

            Optional<CountrySet> countrySet = CountrySet.getByName(countrySetName);
            if (!countrySet.isPresent()) {
                throw new CountryServiceException("Invalid country set name.");
            }

            SOAPMessage soapResponse = spireGetCountriesClient.executeRequest(countrySet.get().getSpireCountrySetId());

            List<Country> countryList = countryListFactory.create(soapResponse);
            countryList.sort((a, b) -> a.getCountryName().compareTo(b.getCountryName()));
            return countryList;

        } catch (SOAPException | UnsupportedEncodingException e) {
            LOGGER.error("SOAP failure - {}", e.getMessage());
            throw new CountryServiceException("Failed to execute SOAP request.", e);
        }
    }


}
