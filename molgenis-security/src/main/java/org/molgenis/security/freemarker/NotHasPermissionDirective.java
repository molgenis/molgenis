package org.molgenis.security.freemarker;

import java.io.IOException;

import org.molgenis.security.core.MolgenisPermissionService;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;

/**
 * Directive that prints the body of the tag if the current user has no permission on entity
 * 
 * usage: <@notHasPermission entity='celiacsprue' permission="WRITE">no write permission</@notHasPermission>
 */
public class NotHasPermissionDirective extends PermissionDirective
{

	public NotHasPermissionDirective(MolgenisPermissionService molgenisPermissionService)
	{
		super(molgenisPermissionService);
	}

	@Override
	protected void execute(boolean hasPermission, Environment env, TemplateDirectiveBody body)
			throws TemplateException, IOException
	{
		if (!hasPermission)
		{
			body.render(env.getOut());
		}
	}

}
