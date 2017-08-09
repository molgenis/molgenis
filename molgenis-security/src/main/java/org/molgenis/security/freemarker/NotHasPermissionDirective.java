package org.molgenis.security.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import org.molgenis.security.core.MolgenisPermissionService;

import java.io.IOException;

/**
 * Directive that prints the body of the tag if the current user has no permission on entity
 * <p>
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
