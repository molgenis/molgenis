package org.molgenis.data.plugin.model;

import org.springframework.security.acls.domain.CumulativePermission;

public class PluginPermissionUtils
{
	private PluginPermissionUtils()
	{
	}

	public static CumulativePermission getCumulativePermission(PluginPermission pluginPermission)
	{
		CumulativePermission cumulativePermission = new CumulativePermission();

		if (pluginPermission.equals(PluginPermission.WRITEMETA))
		{
			cumulativePermission.set(PluginPermission.WRITEMETA)
								.set(PluginPermission.WRITE)
								.set(PluginPermission.READ)
								.set(PluginPermission.COUNT);
		}
		else if (pluginPermission.equals(PluginPermission.WRITE))
		{
			cumulativePermission.set(PluginPermission.WRITE).set(PluginPermission.READ).set(PluginPermission.COUNT);
		}
		else if (pluginPermission.equals(PluginPermission.READ))
		{
			cumulativePermission.set(PluginPermission.READ).set(PluginPermission.COUNT);
		}
		if (pluginPermission.equals(PluginPermission.COUNT))
		{
			cumulativePermission.set(PluginPermission.COUNT);
		}

		return cumulativePermission;
	}
}
