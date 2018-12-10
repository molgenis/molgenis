package org.molgenis.genomebrowser.service;

import static java.util.Objects.requireNonNull;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES;
import static org.molgenis.genomebrowser.meta.GenomeBrowserSettingsMetadata.GENOMEBROWSERSETTINGS;
import static org.molgenis.genomebrowser.service.GenomeBrowserService.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.genomebrowser.GenomeBrowserTrack;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettingsMetadata;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Service implements genomeBrowser specific business logic. */
@Service
@RequestMapping(URI)
public class GenomeBrowserService {
  public static final String API = "/api";
  public static final String URI = API + "/genomebrowser";

  private final DataService dataService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  public GenomeBrowserService(
      DataService dataService, UserPermissionEvaluator userPermissionEvaluator) {
    this.dataService = requireNonNull(dataService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  @GetMapping(value = "/tracks", produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<String> getGenomeBrowserTracksAsString(@RequestParam String entityTypeId) {
    EntityType entityType = dataService.getEntityType(entityTypeId);
    return getTracksString(getGenomeBrowserTracks(entityType));
  }

  public Map<String, GenomeBrowserTrack> getGenomeBrowserTracks(EntityType entityType) {
    return hasPermission()
        ? getGenomeBrowserTracks(
            entityType, getDefaultGenomeBrowserAttributes().collect(Collectors.toList()))
        : Collections.emptyMap();
  }

  /** Get readable genome entities */
  public List<String> getTracksString(Map<String, GenomeBrowserTrack> entityTracks) {
    List<String> results = new ArrayList<>();
    if (hasPermission()) {
      Map<String, GenomeBrowserTrack> allTracks = new HashMap<>(entityTracks);
      for (GenomeBrowserTrack track : entityTracks.values()) {
        allTracks.putAll(getReferenceTracks(track));
      }
      results =
          allTracks
              .values()
              .stream()
              .map(GenomeBrowserTrack::toTrackString)
              .collect(Collectors.toList());
    }
    return results;
  }

  private Map<String, GenomeBrowserTrack> getGenomeBrowserTracks(
      EntityType entityType, List<GenomeBrowserAttributes> defaultGenomeBrowserAttributes) {
    Map<String, GenomeBrowserTrack> settings = new HashMap<>();
    dataService
        .findAll(
            GENOMEBROWSERSETTINGS,
            new QueryImpl<GenomeBrowserSettings>()
                .eq(GenomeBrowserSettingsMetadata.ENTITY, entityType.getIdValue()),
            GenomeBrowserSettings.class)
        .forEach(
            referenceSettings ->
                settings.put(
                    referenceSettings.getIdentifier(),
                    GenomeBrowserTrack.create(referenceSettings)));
    if (settings.isEmpty()) {
      // if not check if attrs match any default config
      Collections.sort(defaultGenomeBrowserAttributes);
      for (GenomeBrowserAttributes genomeBrowserAttributes : defaultGenomeBrowserAttributes) {
        List<String> attributeNames = Lists.newArrayList(entityType.getAttributeNames());
        if (areAllAttributeAvailable(genomeBrowserAttributes, attributeNames)) {
          GenomeBrowserTrack genomeBrowserTrack =
              getDefaultGenomeBrowserSettingsEntity(entityType, genomeBrowserAttributes);
          settings.put(genomeBrowserTrack.getId(), genomeBrowserTrack);
          break;
        }
      }
    }
    return settings;
  }

  Map<String, GenomeBrowserTrack> getReferenceTracks(GenomeBrowserTrack settings) {
    Map<String, GenomeBrowserTrack> result = new HashMap<>();
    if (hasPermission()
        && settings.getMolgenisReferenceMode()
            != GenomeBrowserSettings.MolgenisReferenceMode.NONE) {
      if (settings.getMolgenisReferenceMode()
          == GenomeBrowserSettings.MolgenisReferenceMode.CONFIGURED) {
        // Cannot be null due to nullableExpression on MolgenisReferenceTracks in
        // GenomeBrowserSettingsMetadata
        //noinspection ConstantConditions
        settings
            .getMolgenisReferenceTracks()
            .forEach(referenceTrack -> result.put(referenceTrack.getId(), referenceTrack));
      } else { // Mode == ALL
        // TODO Improve performance by rewriting to query that returns all genomic entities instead
        // of retrieving all entities and determining which one is genomic
        List<GenomeBrowserAttributes> defaultGenomeBrowserAttributes =
            getDefaultGenomeBrowserAttributes().collect(Collectors.toList());
        for (EntityType entityType :
            dataService.getMeta().getEntityTypes().collect(Collectors.toList())) {
          if (!entityType.isAbstract() && !entityType.equals(settings.getEntity())) {
            getGenomeBrowserTracks(entityType, defaultGenomeBrowserAttributes)
                .values()
                .forEach(
                    referenceSettings -> result.put(referenceSettings.getId(), referenceSettings));
          }
        }
      }
    }
    return result;
  }

  private boolean hasPermission() {
    return userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(GENOMEBROWSERSETTINGS), EntityTypePermission.READ_DATA)
        && userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(GENOMEBROWSERATTRIBUTES), EntityTypePermission.READ_DATA);
  }

  private Stream<GenomeBrowserAttributes> getDefaultGenomeBrowserAttributes() {
    return dataService.findAll(
        GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES,
        new QueryImpl<GenomeBrowserAttributes>().eq(GenomeBrowserAttributesMetadata.DEFAULT, true),
        GenomeBrowserAttributes.class);
  }

  private boolean isAttributeAvailable(String attributeName, Iterable<String> attributeNames) {
    return (attributeName == null || Iterables.contains(attributeNames, attributeName));
  }

  private boolean areAllAttributeAvailable(
      GenomeBrowserAttributes genomeBrowserAttributes, Iterable<String> attributeNames) {
    return isAttributeAvailable(genomeBrowserAttributes.getChrom(), attributeNames)
        && isAttributeAvailable(genomeBrowserAttributes.getPos(), attributeNames)
        && isAttributeAvailable(genomeBrowserAttributes.getAlt(), attributeNames)
        && isAttributeAvailable(genomeBrowserAttributes.getRef(), attributeNames)
        && isAttributeAvailable(genomeBrowserAttributes.getStop(), attributeNames);
  }

  private GenomeBrowserTrack getDefaultGenomeBrowserSettingsEntity(
      EntityType entityType, GenomeBrowserAttributes attrs) {
    return GenomeBrowserTrack.create(
        entityType.getIdValue().toString(),
        entityType.getLabel(),
        entityType.getLabelAttribute().getName(),
        entityType,
        GenomeBrowserSettings.TrackType.VARIANT,
        Collections.emptyList(),
        GenomeBrowserSettings.MolgenisReferenceMode.ALL,
        attrs,
        null,
        null,
        null,
        null,
        null);
  }
}
