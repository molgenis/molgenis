package org.molgenis.data.security.exception;

import org.molgenis.security.core.Permission;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

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
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String permissionName = getPermissionName(languageService, permission);
			MessageFormat format = languageService.getMessageFormat(ERROR_CODE);
			return format.format(new Object[] { permissionName, pluginId });
		}).orElseGet(super::getLocalizedMessage);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		throw new UnsupportedOperationException();
	}
}
