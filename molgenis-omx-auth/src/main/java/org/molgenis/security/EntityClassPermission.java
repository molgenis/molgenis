package org.molgenis.security;

public enum EntityClassPermission
{
	READ("read"), WRITE("write"), NONE("none");

	private String permission;

	EntityClassPermission(String permission)
	{
		this.permission = permission;
	}

	@Override
	public String toString()
	{
		return permission;
	}
}
