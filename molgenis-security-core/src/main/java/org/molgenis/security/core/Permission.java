package org.molgenis.security.core;

import org.springframework.security.acls.domain.AbstractPermission;

//FIXME: move to proper module
public class Permission extends AbstractPermission
{
	private String name;

	public static final Permission COUNT = new Permission("COUNT", 1 << 0, 'C'); // 1
	public static final Permission READ = new Permission("READ", 1 << 1, 'R'); // 2
	public static final Permission WRITE = new Permission("WRITE", 1 << 2, 'W'); // 4
	public static final Permission WRITEMETA = new Permission("WRITEMETA", 1 << 3, 'M'); // 8

	protected Permission(String name, int mask)
	{
		super(mask);
		this.name = name;
	}

	public Permission(String name, int mask, char code)
	{
		super(mask, code);
		this.name = name;
	}
}
