package org.molgenis.data.security;

import org.springframework.security.acls.domain.AbstractPermission;

public class PackagePermission extends AbstractPermission
{
	private final String name;

	public static final PackagePermission COUNT = new PackagePermission("COUNT", 1 << 0, 'C'); // 1
	public static final PackagePermission READ = new PackagePermission("READ", 1 << 1, 'R'); // 2
	public static final PackagePermission WRITE = new PackagePermission("WRITE", 1 << 2, 'W'); // 4
	public static final PackagePermission WRITEMETA = new PackagePermission("WRITEMETA", 1 << 3, 'M'); // 8

	protected PackagePermission(String name, int mask)
	{
		super(mask);
		this.name = name;
	}

	protected PackagePermission(String name, int mask, char code)
	{
		super(mask, code);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
