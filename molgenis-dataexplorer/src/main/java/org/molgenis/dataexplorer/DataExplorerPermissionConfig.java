package org.molgenis.dataexplorer;

import org.molgenis.security.core.PermissionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.PermissionSet.*;

@Import(PermissionRegistry.class)
@Configuration
public class DataExplorerPermissionConfig
{
	private final PermissionRegistry permissionRegistry;

	public DataExplorerPermissionConfig(PermissionRegistry permissionRegistry)
	{
		this.permissionRegistry = requireNonNull(permissionRegistry);
	}

	@PostConstruct
	public void registerPermissions()
	{
		permissionRegistry.addMapping(EntityTypeReportPermission.VIEW_REPORT, READ, WRITE, WRITEMETA);
		permissionRegistry.addMapping(EntityTypeReportPermission.MANAGE_REPORT, WRITE, WRITEMETA);
	}
}
