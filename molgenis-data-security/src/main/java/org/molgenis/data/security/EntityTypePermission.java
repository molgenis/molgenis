package org.molgenis.data.security;

import org.springframework.security.acls.domain.AbstractPermission;

public class EntityTypePermission extends AbstractPermission
{
	public static final EntityTypePermission COUNT = new EntityTypePermission(1 << 0, 'C'); // 1
	public static final EntityTypePermission READ = new EntityTypePermission(1 << 1, 'R'); // 2
	public static final EntityTypePermission WRITE = new EntityTypePermission(1 << 2, 'W'); // 4
	public static final EntityTypePermission WRITEMETA = new EntityTypePermission(1 << 3, 'M'); // 8

	protected EntityTypePermission(int mask)
	{
		super(mask);
	}

	protected EntityTypePermission(int mask, char code)
	{
		super(mask, code);
	}
}
