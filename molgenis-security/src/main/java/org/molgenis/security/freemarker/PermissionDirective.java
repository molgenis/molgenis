package org.molgenis.security.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;
import org.molgenis.data.DataConverter;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.model.Permission;

import java.io.IOException;
import java.util.Map;

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
					toEntityTypePermission(permissionValue));
		}

		if ((pluginIdValue != null) && hasPermission)
		{
			hasPermission = permissionService.hasPermission(new PluginIdentity(pluginIdValue),
					toPluginPermission(permissionValue));
		}

		execute(hasPermission, env, body);
	}

	protected abstract void execute(boolean hasPermission, Environment env, TemplateDirectiveBody body)
			throws TemplateException, IOException;

	private Permission toEntityTypePermission(String permission)
	{
		switch (permission)
		{
			case "COUNT":
				return EntityTypePermission.COUNT;
			case "READ":
				return EntityTypePermission.READ;
			case "WRITE":
				return EntityTypePermission.WRITE;
			case "WRITEMETA":
				return EntityTypePermission.WRITEMETA;
			case "NONE":
				throw new IllegalArgumentException(
						format("Permission evaluation for permission '%s' not allowed", permission));
			default:
				throw new IllegalArgumentException(format("Unknown permission '%s'", permission));
		}
	}

	private Permission toPluginPermission(String permission)
	{
		switch (permission)
		{
			case "READ":
				return PluginPermission.READ;
			case "NONE":
				throw new IllegalArgumentException(
						format("Permission evaluation for permission '%s' not allowed", permission));
			default:
				throw new IllegalArgumentException(format("Unknown permission '%s'", permission));
		}
	}
}
