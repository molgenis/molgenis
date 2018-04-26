package org.molgenis.data.security;

import org.springframework.security.acls.domain.AbstractPermission;

public class EntityPermission extends AbstractPermission
{
	private final String name;
	public static final EntityPermission COUNT = new EntityPermission("COUNT", 1 << 0, 'C'); // 1
	public static final EntityPermission READ = new EntityPermission("READ", 1 << 1, 'R'); // 2
	public static final EntityPermission WRITE = new EntityPermission("WRITE", 1 << 2, 'W'); // 4

	protected EntityPermission(String name, int mask)
	{
		super(mask);
		this.name = name;
	}

	protected EntityPermission(String name, int mask, char code)
	{
		super(mask, code);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
