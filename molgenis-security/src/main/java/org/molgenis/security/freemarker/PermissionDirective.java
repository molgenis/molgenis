package org.molgenis.security.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;
import org.molgenis.data.DataConverter;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;

import java.io.IOException;
import java.util.Map;

public abstract class PermissionDirective implements TemplateDirectiveModel
{
	private final MolgenisPermissionService molgenisPermissionService;

	public PermissionDirective(MolgenisPermissionService molgenisPermissionService)
	{
		this.molgenisPermissionService = molgenisPermissionService;
	}

	@Override
	public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException
	{
		String entityTypeId = DataConverter.toString(params.get("entityTypeId"));
		String plugin = DataConverter.toString(params.get("plugin"));
		String permission = DataConverter.toString(params.get("permission"));

		if (permission == null) throw new TemplateModelException("Missing 'permission' parameter");
		if ((entityTypeId == null) && (plugin == null))
			throw new TemplateModelException("Missing 'entityTypeId' and/or 'plugin' parameter");

		boolean hasPermission = true;
		if (entityTypeId != null)
		{
			hasPermission = molgenisPermissionService.hasPermissionOnEntity(entityTypeId,
					Permission.valueOf(permission));
		}

		if ((plugin != null) && hasPermission)
		{
			hasPermission = molgenisPermissionService.hasPermissionOnPlugin(plugin, Permission.valueOf(permission));
		}

		execute(hasPermission, env, body);
	}

	protected abstract void execute(boolean hasPermission, Environment env, TemplateDirectiveBody body)
			throws TemplateException, IOException;
}
