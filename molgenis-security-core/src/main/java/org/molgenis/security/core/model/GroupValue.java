package org.molgenis.security.core.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GroupValue {
  public abstract String getName();

  public abstract String getLabel();

  @Nullable
  public abstract String getDescription();

  public abstract boolean isPublic();

  public abstract ImmutableList<RoleValue> getRoles();

  public abstract PackageValue getRootPackage();

  public static Builder builder() {
    return new AutoValue_GroupValue.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String value);

    public abstract Builder setLabel(String value);

    public abstract Builder setDescription(String value);

    public abstract Builder setPublic(boolean value);

    public abstract ImmutableList.Builder<RoleValue> rolesBuilder();

    public abstract Builder setRootPackage(PackageValue packageValue);

    public abstract GroupValue build();
  }
}
