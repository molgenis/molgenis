package org.molgenis.data.plugin.model;

import org.springframework.security.acls.domain.AbstractPermission;

public class PluginPermission extends AbstractPermission
{
	public static final PluginPermission READ = new PluginPermission(1 << 0, 'R'); // 1
	public static final PluginPermission WRITE = new PluginPermission(1 << 1, 'W'); // 2
	/**
	 * @deprecated use {@link #READ} or {@link #WRITE}
	 */
	@Deprecated
	public static final PluginPermission COUNT = new PluginPermission(1 << 2, 'Y'); // 4
	/**
	 * @deprecated use {@link #READ} or {@link #WRITE}
	 */
	@Deprecated
	public static final PluginPermission WRITEMETA = new PluginPermission(1 << 3, 'Z'); // 8

	protected PluginPermission(int mask)
	{
		super(mask);
	}

	protected PluginPermission(int mask, char code)
	{
		super(mask, code);
	}
}
