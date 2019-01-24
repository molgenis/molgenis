package org.molgenis.beacon.config;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.beacon.config.BeaconMetadata.API_VERSION;
import static org.molgenis.beacon.config.BeaconMetadata.BEACON_ORGANIZATION;
import static org.molgenis.beacon.config.BeaconMetadata.DATA_SETS;
import static org.molgenis.beacon.config.BeaconMetadata.DESCRIPTION;
import static org.molgenis.beacon.config.BeaconMetadata.ID;
import static org.molgenis.beacon.config.BeaconMetadata.NAME;
import static org.molgenis.beacon.config.BeaconMetadata.VERSION;
import static org.molgenis.beacon.config.BeaconMetadata.WELCOME_URL;

import java.util.List;
import java.util.Optional;
import org.molgenis.beacon.controller.model.BeaconDatasetResponse;
import org.molgenis.beacon.controller.model.BeaconOrganizationResponse;
import org.molgenis.beacon.controller.model.BeaconResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class Beacon extends StaticEntity {
  public Beacon(Entity entity) {
    super(entity);
  }

  public Beacon(EntityType entityType) {
    super(entityType);
  }

  public Beacon(String identifier, EntityType entityType) {
    super(identifier, entityType);
  }

  public String getId() {
    return getString(ID);
  }

  public String getName() {
    return getString(NAME);
  }

  public String getApiVersion() {
    return getString(API_VERSION);
  }

  public Optional<BeaconOrganization> getOrganization() {
    return Optional.ofNullable(getEntity(BEACON_ORGANIZATION, BeaconOrganization.class));
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(getString(DESCRIPTION));
  }

  public Optional<String> getVersion() {
    return Optional.ofNullable(getString(VERSION));
  }

  public Optional<String> getWelcomeUrl() {
    return Optional.ofNullable(getString(WELCOME_URL));
  }

  public Iterable<BeaconDataset> getDataSets() {
    return getEntities(DATA_SETS, BeaconDataset.class);
  }

  public BeaconResponse toBeaconResponse() {
    BeaconOrganizationResponse beaconOrganizationResponse =
        getOrganization().map(BeaconOrganization::toBeaconOrganizationResponse).orElse(null);

    return BeaconResponse.create(
        getId(),
        getName(),
        getApiVersion(),
        beaconOrganizationResponse,
        getDescription().orElse(null),
        getVersion().orElse(null),
        getWelcomeUrl().orElse(null),
        entityTypeToBeaconDataset());
  }

  private List<BeaconDatasetResponse> entityTypeToBeaconDataset() {
    List<BeaconDatasetResponse> beaconDatasets = newArrayList();
    getDataSets()
        .forEach(
            dataset ->
                beaconDatasets.add(
                    BeaconDatasetResponse.create(
                        dataset.getId(), dataset.getLabel(), dataset.getDescription())));
    return beaconDatasets;
  }
}
