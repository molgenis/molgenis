package org.molgenis.data.security;

import org.springframework.security.acls.domain.AbstractPermission;

public class PackagePermission extends AbstractPermission
{
	public static final PackagePermission COUNT = new PackagePermission(1 << 0, 'C'); // 1
	public static final PackagePermission READ = new PackagePermission(1 << 1, 'R'); // 2
	public static final PackagePermission WRITE = new PackagePermission(1 << 2, 'W'); // 4
	public static final PackagePermission CREATE = new PackagePermission(1 << 3, 'X'); // 8
	public static final PackagePermission WRITEMETA = new PackagePermission(1 << 4, 'M'); // 16

	public PackagePermission(int mask)
	{
		super(mask);
	}

	protected PackagePermission(int mask, char code)
	{
		super(mask, code);
	}
}
