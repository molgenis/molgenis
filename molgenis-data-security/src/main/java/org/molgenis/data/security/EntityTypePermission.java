package org.molgenis.data.security;

import org.springframework.security.acls.domain.AbstractPermission;

public class EntityTypePermission extends AbstractPermission
{
	private final String name;

	public static final EntityTypePermission COUNT = new EntityTypePermission("COUNT", 1 << 0, 'C'); // 1
	public static final EntityTypePermission READ = new EntityTypePermission("READ", 1 << 1, 'R'); // 2
	public static final EntityTypePermission WRITE = new EntityTypePermission("WRITE", 1 << 2, 'W'); // 4
	public static final EntityTypePermission WRITEMETA = new EntityTypePermission("WRITEMETA", 1 << 3, 'M'); // 8

	protected EntityTypePermission(String name, int mask)
	{
		super(mask);
		this.name = name;
	}

	protected EntityTypePermission(String name, int mask, char code)
	{
		super(mask, code);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
