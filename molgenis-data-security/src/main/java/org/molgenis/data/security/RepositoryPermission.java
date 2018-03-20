package org.molgenis.data.security;

import org.springframework.security.acls.domain.AbstractPermission;

public class RepositoryPermission extends AbstractPermission
{
	public static final RepositoryPermission COUNT = new RepositoryPermission(1 << 0, 'C'); // 1
	public static final RepositoryPermission READ = new RepositoryPermission(1 << 1, 'R'); // 2
	public static final RepositoryPermission WRITE = new RepositoryPermission(1 << 2, 'W'); // 4
	public static final RepositoryPermission WRITEMETA = new RepositoryPermission(1 << 3, 'M'); // 8

	public RepositoryPermission(int mask)
	{
		super(mask);
	}

	protected RepositoryPermission(int mask, char code)
	{
		super(mask, code);
	}
}
