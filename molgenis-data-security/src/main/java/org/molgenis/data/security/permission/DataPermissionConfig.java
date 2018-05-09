package org.molgenis.data.security.permission;

import org.molgenis.data.security.EntityPermission;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.PackagePermission;
import org.molgenis.security.core.PermissionRegistry;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.PermissionSet.*;

@Configuration
public class DataPermissionConfig
{
	private final PermissionRegistry permissionRegistry;

	public DataPermissionConfig(PermissionRegistry permissionRegistry)
	{
		this.permissionRegistry = requireNonNull(permissionRegistry);
	}

	@PostConstruct
	public void registerPermissions()
	{
		permissionRegistry.addMapping(PackagePermission.ADD_PACKAGE, WRITEMETA);
		permissionRegistry.addMapping(PackagePermission.ADD_ENTITY_TYPE, WRITEMETA);

		permissionRegistry.addMapping(EntityTypePermission.READ_METADATA, COUNT, READ, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityTypePermission.COUNT_DATA, COUNT, READ, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityTypePermission.AGGREGATE_DATA, COUNT, READ, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityTypePermission.READ_DATA, READ, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityTypePermission.ADD_DATA, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityTypePermission.UPDATE_DATA, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityTypePermission.DELETE_DATA, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityTypePermission.UPDATE_METADATA, WRITEMETA);
		permissionRegistry.addMapping(EntityTypePermission.DELETE_METADATA, WRITEMETA);

		permissionRegistry.addMapping(EntityPermission.READ, READ, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityPermission.UPDATE, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityPermission.DELETE, WRITE, WRITEMETA);
	}
}
