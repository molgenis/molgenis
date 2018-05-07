package org.molgenis.security.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;
import org.molgenis.data.DataConverter;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.UserPermissionEvaluator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

public abstract class PermissionDirective implements TemplateDirectiveModel
{
	private final UserPermissionEvaluator permissionService;

	PermissionDirective(UserPermissionEvaluator permissionService)
	{
		this.permissionService = permissionService;
	}

	@Override
	public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException
	{
		String entityTypeIdValue = DataConverter.toString(params.get("entityTypeId"));
		String pluginIdValue = DataConverter.toString(params.get("plugin"));
		String permissionValue = DataConverter.toString(params.get("permission"));

		if (permissionValue == null) throw new TemplateModelException("Missing 'permission' parameter");
		if ((entityTypeIdValue == null) && (pluginIdValue == null))
			throw new TemplateModelException("Missing 'entityTypeId' and/or 'plugin' parameter");

		boolean hasPermission = true;
		if (entityTypeIdValue != null)
		{
			hasPermission = permissionService.hasPermission(new EntityTypeIdentity(entityTypeIdValue),
					toEntityTypePermissions(permissionValue));
		}

		if ((pluginIdValue != null) && hasPermission)
		{
			hasPermission = permissionService.hasPermission(new PluginIdentity(pluginIdValue),
					PluginPermission.VIEW_PLUGIN);
		}

		execute(hasPermission, env, body);
	}

	protected abstract void execute(boolean hasPermission, Environment env, TemplateDirectiveBody body)
			throws TemplateException, IOException;

	private List<Permission> toEntityTypePermissions(String permission)
	{
		switch (permission)
		{
			case "COUNT":
				return newArrayList(EntityTypePermission.COUNT_DATA);
			case "READ":
				return newArrayList(EntityTypePermission.READ_METADATA, EntityTypePermission.READ_DATA);
			case "WRITE":
				return newArrayList(EntityTypePermission.READ_METADATA, EntityTypePermission.UPDATE_DATA);
			case "WRITEMETA":
				return newArrayList(EntityTypePermission.UPDATE_METADATA);
			case "NONE":
				throw new IllegalArgumentException(
						format("Permission evaluation for permission '%s' not allowed", permission));
			default:
				throw new IllegalArgumentException(format("Unknown permission '%s'", permission));
		}
	}
}
