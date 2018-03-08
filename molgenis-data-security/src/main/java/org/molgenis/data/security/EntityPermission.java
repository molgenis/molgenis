package org.molgenis.data.security;

import org.springframework.security.acls.domain.AbstractPermission;

public class EntityPermission extends AbstractPermission
{
	public static final EntityPermission COUNT = new EntityPermission(1 << 0, 'C'); // 1
	public static final EntityPermission READ = new EntityPermission(1 << 1, 'R'); // 2
	public static final EntityPermission WRITE = new EntityPermission(1 << 2, 'W'); // 4

	protected EntityPermission(int mask)
	{
		super(mask);
	}

	protected EntityPermission(int mask, char code)
	{
		super(mask, code);
	}
}
