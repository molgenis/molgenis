package org.molgenis.security.freemarker;

import java.io.IOException;

import org.molgenis.security.core.MolgenisPermissionService;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;

/**
 * Directive that prints the body of the tag if the current user has permission on entity
 * 
 * usage: <@hasPermission entityName='celiacsprue' permission="WRITE">write permission</@hasPermission>
 */
public class HasPermissionDirective extends PermissionDirective
{
	public HasPermissionDirective(MolgenisPermissionService molgenisPermissionService)
	{
		super(molgenisPermissionService);
	}

	@Override
	protected void execute(boolean hasPermission, Environment env, TemplateDirectiveBody body)
			throws TemplateException, IOException
	{
		if (hasPermission)
		{
			body.render(env.getOut());
		}
	}

}
