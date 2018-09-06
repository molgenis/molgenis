package org.molgenis.core.ui.admin.permission;

import static java.util.Objects.requireNonNull;

import javax.validation.constraints.NotNull;

public class EntityTypeRlsRequest {
  @NotNull private final String id;
  private final boolean rlsEnabled;

  public EntityTypeRlsRequest(String id, boolean rlsEnabled) {
    this.id = requireNonNull(id);
    this.rlsEnabled = rlsEnabled;
  }

  public String getId() {
    return id;
  }

  public boolean isRlsEnabled() {
    return rlsEnabled;
  }
}
