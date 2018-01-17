package org.molgenis.data.security.exception;

import org.molgenis.security.core.Permission;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class PluginPermissionDeniedException extends PermissionDeniedException
{
	private static final String ERROR_CODE = "S05";

	private final String pluginId;
	private final Permission permission;

	public PluginPermissionDeniedException(String pluginId, Permission permission)
	{
		super(ERROR_CODE);
		this.pluginId = pluginId;
		this.permission = permission;
	}

	public PluginPermissionDeniedException(String pluginId, Permission permission, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.pluginId = pluginId;
		this.permission = permission;
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s permission:%s", pluginId, permission.name());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { getMessageSourceResolvable(permission), pluginId };
	}
}
