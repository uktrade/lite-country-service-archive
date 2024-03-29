package uk.gov.bis.lite.countryservice.resource;

import io.dropwizard.auth.Auth;
import uk.gov.bis.lite.common.auth.basic.Roles;
import uk.gov.bis.lite.common.auth.basic.User;
import uk.gov.bis.lite.countryservice.api.CountryData;
import uk.gov.bis.lite.countryservice.api.CountryView;
import uk.gov.bis.lite.countryservice.exception.BadRequestException;
import uk.gov.bis.lite.countryservice.exception.CountryRefNotFoundException;
import uk.gov.bis.lite.countryservice.service.CountryDataValidationService;
import uk.gov.bis.lite.countryservice.service.CountryService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/country-data")
public class CountryDataResource {

  private final CountryService countryService;
  private final CountryDataValidationService countryDataValidationService;

  @Inject
  public CountryDataResource(CountryService countryService, CountryDataValidationService countryDataValidationService) {
    this.countryService = countryService;
    this.countryDataValidationService = countryDataValidationService;
  }

  @RolesAllowed(Roles.SERVICE)
  @GET
  @Path("/{countryRef}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCountryData(@Auth User user,
                                 @PathParam("countryRef") String countryRef) {
    Optional<CountryView> countryView = countryService.getCountryView(countryRef);
    if (countryView.isPresent()) {
      return Response.ok(countryView.get()).build();
    } else {
      throw new CountryRefNotFoundException("The following countryRef does not exist: " + countryRef);
    }
  }

  @RolesAllowed(Roles.ADMIN)
  @PUT
  @Path("/{countryRef}")
  public Response updateCountryData(@Auth User user,
                                    @PathParam("countryRef") String countryRef,
                                    @NotNull CountryData countryData) {
    CountryData updateCountryData = new CountryData(countryRef, countryData.getSynonyms());
    List<CountryData> updateCountryDataList = Collections.singletonList(updateCountryData);
    List<String> unmatchedCountryRefs = countryDataValidationService.getUnmatchedCountryRefs(updateCountryDataList);
    List<String> countryRefsWithBlankSynonyms = countryDataValidationService.getCountryRefsWithBlankSynonyms(updateCountryDataList);
    List<String> countryRefsWithDuplicateSynonyms = countryDataValidationService.getCountryRefsWithDuplicateSynonyms(updateCountryDataList);
    if (!unmatchedCountryRefs.isEmpty()) {
      throw new CountryRefNotFoundException("The following countryRef does not exist: " + unmatchedCountryRefs.get(0));
    } else if (!countryRefsWithBlankSynonyms.isEmpty()) {
      throw new BadRequestException("The following countryRef contains null or empty synonyms: " + countryRefsWithBlankSynonyms.get(0));
    } else if (!countryRefsWithDuplicateSynonyms.isEmpty()) {
      throw new BadRequestException("The following countryRef contains duplicate synonyms: " + countryRefsWithDuplicateSynonyms.get(0));
    } else {
      countryService.bulkUpdateCountryData(updateCountryDataList);
      return Response.ok().build();
    }
  }

  @RolesAllowed(Roles.ADMIN)
  @DELETE
  @Path("/{countryRef}")
  public Response deleteCountryData(@Auth User user,
                                    @PathParam("countryRef") String countryRef) {
    countryService.deleteCountryData(countryRef);
    return Response.accepted().build();
  }

  @RolesAllowed(Roles.SERVICE)
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllCountryData(@Auth User user) {
    List<CountryView> countryViews = countryService.getCountryViews();
    return Response.ok(countryViews).build();
  }

  @RolesAllowed(Roles.ADMIN)
  @PUT
  public Response bulkUpdateCountryData(@Auth User user,
                                        @NotNull List<CountryData> countryDataList) {
    if (countryDataList.contains(null)) {
      throw new BadRequestException("Array cannot contain null values.");
    }
    List<String> unmatchedCountryRefs = countryDataValidationService.getUnmatchedCountryRefs(countryDataList);
    Set<String> duplicateCountryRefs = countryDataValidationService.getDuplicateCountryRefs(countryDataList);
    List<String> countryRefsWithBlankSynonyms = countryDataValidationService.getCountryRefsWithBlankSynonyms(countryDataList);
    List<String> countryRefsWithDuplicateSynonyms = countryDataValidationService.getCountryRefsWithDuplicateSynonyms(countryDataList);
    if (!unmatchedCountryRefs.isEmpty()) {
      throw new CountryRefNotFoundException("The following countryRef do not exist: " + String.join(", ", unmatchedCountryRefs));
    } else if (!duplicateCountryRefs.isEmpty()) {
      throw new BadRequestException("The following countryRef are duplicate: " + String.join(", ", duplicateCountryRefs));
    } else if (!countryRefsWithBlankSynonyms.isEmpty()) {
      throw new BadRequestException("The following countryRef contain null or empty synonyms: " + String.join(", ", countryRefsWithBlankSynonyms));
    } else if (!countryRefsWithDuplicateSynonyms.isEmpty()) {
      throw new BadRequestException("The following countryRef contain duplicate synonyms: " + String.join(", ", countryRefsWithDuplicateSynonyms));
    } else {
      countryService.bulkUpdateCountryData(countryDataList);
      return Response.ok().build();
    }
  }

  @RolesAllowed(Roles.ADMIN)
  @DELETE
  public Response deleteAllCountryData(@Auth User user) {
    countryService.deleteAllCountryData();
    return Response.accepted().build();
  }

}
