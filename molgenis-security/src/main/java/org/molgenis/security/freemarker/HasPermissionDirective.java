package org.molgenis.security.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import org.molgenis.security.core.UserPermissionEvaluator;

import java.io.IOException;

/**
 * Directive that prints the body of the tag if the current user has permission on entity
 * <p>
 * usage: <@hasPermission entityTypeId='celiacsprue' permission="WRITE">write permission</@hasPermission>
 */
public class HasPermissionDirective extends PermissionDirective
{
	public HasPermissionDirective(UserPermissionEvaluator permissionService)
	{
		super(permissionService);
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
