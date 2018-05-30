package org.molgenis.security.core;

import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.PackageValue;
import org.molgenis.security.core.model.RoleValue;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class GroupValueFactory
{
	private static final String NON_ALPHA_NUMERIC = "[^a-zA-Z0-9\\u00C0-\\u00FF]";

	public GroupValue createGroup(@Nullable String name, String label, @Nullable String description,
			boolean publiclyVisible, Collection<String> roleLabels)
	{
		final String groupName = Optional.ofNullable(name).orElse(createGroupName(label));
		GroupValue.Builder groupBuilder = GroupValue.builder()
													.setName(groupName)
													.setLabel(label)
													.setPublic(publiclyVisible);
		Optional.ofNullable(description).ifPresent(groupBuilder::setDescription);

		PackageValue.Builder packageBuilder = PackageValue.builder()
														  .setName(groupName.replace('-', '_'))
														  .setLabel(label);
		Optional.ofNullable(description).ifPresent(packageBuilder::setDescription);
		PackageValue rootPackage = packageBuilder.build();
		groupBuilder.setRootPackage(rootPackage);

		List<RoleValue> roles = roleLabels.stream()
										  .map(roleLabel -> create(groupName, label, roleLabel))
										  .collect(toList());
		groupBuilder.rolesBuilder().addAll(roles);

		return groupBuilder.build();
	}

	private static String createGroupName(String label)
	{
		return label.replaceAll(NON_ALPHA_NUMERIC, "_").toLowerCase();
	}

	private static String createRoleName(String groupName, String roleLabel)
	{
		return (groupName + " " + roleLabel).toUpperCase().replaceAll(NON_ALPHA_NUMERIC, "_");
	}

	public static RoleValue create(String groupName, String groupLabel, String roleLabel)
	{
		return RoleValue.builder()
						.setName(createRoleName(groupName, roleLabel))
						.setLabel(roleLabel)
						.setDescription(groupLabel + " " + roleLabel)
						.build();
	}
}