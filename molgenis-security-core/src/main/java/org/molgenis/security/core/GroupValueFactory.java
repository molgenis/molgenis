package org.molgenis.security.core;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.PackageValue;
import org.molgenis.security.core.model.RoleValue;
import org.springframework.stereotype.Component;

@Component
public class GroupValueFactory {
  public GroupValue createGroup(
      String groupName,
      String label,
      @Nullable String description,
      boolean publiclyVisible,
      Collection<String> roleLabels) {
    GroupValue.Builder groupBuilder =
        GroupValue.builder().setName(groupName).setLabel(label).setPublic(publiclyVisible);
    Optional.ofNullable(description).ifPresent(groupBuilder::setDescription);

    PackageValue.Builder packageBuilder =
        PackageValue.builder().setName(groupName.replace('-', '_')).setLabel(label);
    Optional.ofNullable(description).ifPresent(packageBuilder::setDescription);
    PackageValue rootPackage = packageBuilder.build();
    groupBuilder.setRootPackage(rootPackage);

    ImmutableList.Builder<RoleValue> rolesBuilder = groupBuilder.rolesBuilder();
    roleLabels
        .stream()
        .map(roleLabel -> createRoleValue(groupName, label, roleLabel))
        .forEach(rolesBuilder::add);
    return groupBuilder.build();
  }

  public GroupValue createGroup(String groupName, String label, Collection<String> roleLabels) {
    return this.createGroup(groupName, label, null, true, roleLabels);
  }

  public static String createRoleName(String groupName, String roleLabel) {
    return (groupName + " " + roleLabel).replaceAll("[- ]+", "_").toUpperCase();
  }

  private static RoleValue createRoleValue(String groupName, String groupLabel, String roleLabel) {
    return RoleValue.builder()
        .setName(createRoleName(groupName, roleLabel))
        .setLabel(roleLabel)
        .setDescription(groupLabel + " " + roleLabel)
        .build();
  }
}
