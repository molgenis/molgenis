package org.molgenis.data.plugin.model;

import org.springframework.security.acls.domain.AbstractPermission;

public class PluginPermission extends AbstractPermission
{
	public static final PluginPermission READ = new PluginPermission(1 << 0, 'R'); // 1

	protected PluginPermission(int mask)
	{
		super(mask);
	}

	protected PluginPermission(int mask, char code)
	{
		super(mask, code);
	}
}
