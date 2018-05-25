package org.molgenis.security.core;

import com.google.common.collect.ImmutableList;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.PackageValue;
import org.molgenis.security.core.model.RoleValue;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class GroupValueFactoryTest
{
	private GroupValueFactory groupValueFactory = new GroupValueFactory();

	@Test
	public void testCreateGroup()
	{
		GroupValue bbmri_eric = groupValueFactory.createGroup(null, "BBMRI Eric",
				"The world's largest biobank catalogue", true, ImmutableList.of("Manager", "Editor", "Viewer"));

		RoleValue manager = RoleValue.builder()
									 .setName("BBMRI_ERIC_MANAGER")
									 .setLabel("Manager")
									 .setDescription("BBMRI Eric Manager")
									 .build();

		RoleValue editor = RoleValue.builder()
									.setName("BBMRI_ERIC_EDITOR")
									.setLabel("Editor")
									.setDescription("BBMRI Eric Editor")
									.build();

		RoleValue viewer = RoleValue.builder()
									.setName("BBMRI_ERIC_VIEWER")
									.setLabel("Viewer")
									.setDescription("BBMRI Eric Viewer")
									.build();

		PackageValue rootPackage = PackageValue.builder().setName("bbmri_eric")
											   .setLabel("BBMRI Eric")
											   .setDescription("The world's largest biobank catalogue")
											   .build();

		GroupValue.Builder expectedBuilder = GroupValue.builder().setName("bbmri_eric")
													   .setLabel("BBMRI Eric")
													   .setDescription("The world's largest biobank catalogue")
													   .setPublic(true)
													   .setRootPackage(rootPackage);

		expectedBuilder.rolesBuilder().add(manager).add(editor).add(viewer);

		assertEquals(bbmri_eric, expectedBuilder.build());
	}
}