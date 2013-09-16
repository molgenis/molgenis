package org.molgenis.security;

public enum PluginPermission
{
	READ("read"), WRITE("write"), NONE("none");

	private String permission;

	PluginPermission(String permission)
	{
		this.permission = permission;
	}

	@Override
	public String toString()
	{
		return permission;
	}
}
