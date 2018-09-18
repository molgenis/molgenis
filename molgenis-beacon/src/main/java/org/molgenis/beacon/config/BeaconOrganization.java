package org.molgenis.beacon.config;

import static org.molgenis.beacon.config.BeaconOrganizationMetadata.ADDRESS;
import static org.molgenis.beacon.config.BeaconOrganizationMetadata.CONTACT_URL;
import static org.molgenis.beacon.config.BeaconOrganizationMetadata.DESCRIPTION;
import static org.molgenis.beacon.config.BeaconOrganizationMetadata.ID;
import static org.molgenis.beacon.config.BeaconOrganizationMetadata.LOGO_URL;
import static org.molgenis.beacon.config.BeaconOrganizationMetadata.NAME;
import static org.molgenis.beacon.config.BeaconOrganizationMetadata.WELCOME_URL;

import javax.annotation.Nullable;
import org.molgenis.beacon.controller.model.BeaconOrganizationResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class BeaconOrganization extends StaticEntity {
  public BeaconOrganization(Entity entity) {
    super(entity);
  }

  public BeaconOrganization(EntityType entityType) {
    super(entityType);
  }

  public BeaconOrganization(String identifier, EntityType entityType) {
    super(identifier, entityType);
  }

  public String getId() {
    return getString(ID);
  }

  public String getName() {
    return getString(NAME);
  }

  @Nullable
  public String getDescription() {
    return getString(DESCRIPTION);
  }

  @Nullable
  public String getAddress() {
    return getString(ADDRESS);
  }

  @Nullable
  public String getWelcomeUrl() {
    return getString(WELCOME_URL);
  }

  @Nullable
  public String getContactUrl() {
    return getString(CONTACT_URL);
  }

  @Nullable
  public String getLogoUrl() {
    return getString(LOGO_URL);
  }

  public BeaconOrganizationResponse toBeaconOrganizationResponse() {
    return BeaconOrganizationResponse.create(
        getId(),
        getName(),
        getDescription(),
        getAddress(),
        getWelcomeUrl(),
        getContactUrl(),
        getLogoUrl());
  }
}
