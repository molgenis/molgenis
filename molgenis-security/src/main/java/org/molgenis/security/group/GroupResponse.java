package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.data.security.auth.Group;

@AutoValue
@SuppressWarnings("squid:S1610")
public abstract class GroupResponse {
  public abstract String getName();

  public abstract String getLabel();

  static GroupResponse create(String name, String label) {
    return new AutoValue_GroupResponse(name, label);
  }

  static GroupResponse fromEntity(Group groupEntity) {
    return GroupResponse.create(groupEntity.getName(), groupEntity.getLabel());
  }
}
