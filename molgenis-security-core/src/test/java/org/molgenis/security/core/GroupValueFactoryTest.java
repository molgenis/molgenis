package org.molgenis.security.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.security.core.GroupValueFactory.createRoleName;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.PackageValue;
import org.molgenis.security.core.model.RoleValue;

public class GroupValueFactoryTest {
  private GroupValueFactory groupValueFactory = new GroupValueFactory();

  @Test
  void testCreateGroup() {
    GroupValue bbmri_eric =
        groupValueFactory.createGroup(
            "bbmri_eric",
            "BBMRI Eric",
            "The world's largest biobank catalogue",
            true,
            ImmutableList.of("Manager", "Editor", "Viewer"),
            "bbmri_eric");

    GroupValue groupValue = getTestGroupValue();
    assertEquals(groupValue, bbmri_eric);
  }

  /** Creates a test GroupValue. */
  public static GroupValue getTestGroupValue() {
    RoleValue manager =
        RoleValue.builder()
            .setName("bbmri_eric_MANAGER")
            .setLabel("Manager")
            .setDescription("BBMRI Eric Manager")
            .build();

    RoleValue editor =
        RoleValue.builder()
            .setName("bbmri_eric_EDITOR")
            .setLabel("Editor")
            .setDescription("BBMRI Eric Editor")
            .build();

    RoleValue viewer =
        RoleValue.builder()
            .setName("bbmri_eric_VIEWER")
            .setLabel("Viewer")
            .setDescription("BBMRI Eric Viewer")
            .build();

    PackageValue rootPackage =
        PackageValue.builder()
            .setName("bbmri_eric")
            .setLabel("BBMRI Eric")
            .setDescription("The world's largest biobank catalogue")
            .build();

    GroupValue.Builder expectedBuilder =
        GroupValue.builder()
            .setName("bbmri_eric")
            .setLabel("BBMRI Eric")
            .setDescription("The world's largest biobank catalogue")
            .setPublic(true)
            .setRootPackage(rootPackage);

    expectedBuilder.rolesBuilder().add(manager).add(editor).add(viewer);

    return expectedBuilder.build();
  }

  @ParameterizedTest
  @MethodSource("roleNameProvider")
  void testCreateRoleName(String groupName, String roleLabel, String roleName) {
    assertEquals(roleName, createRoleName(groupName, roleLabel));
  }

  static Object[][] roleNameProvider() {
    return new Object[][] {
      new Object[] {"bbmri_eric", "Manager", "bbmri_eric_MANAGER"},
      new Object[] {"patientenfederatie-nederland", "Member", "patientenfederatie-nederland_MEMBER"}
    };
  }
}
