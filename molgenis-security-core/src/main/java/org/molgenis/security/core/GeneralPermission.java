package org.molgenis.security.core;

import org.springframework.security.acls.domain.AbstractPermission;

//FIXME: rename ang move to proper module
public class GeneralPermission extends AbstractPermission
{
	private String name;

	public static final GeneralPermission COUNT = new GeneralPermission("COUNT", 1 << 0, 'C'); // 1
	public static final GeneralPermission READ = new GeneralPermission("READ", 1 << 1, 'R'); // 2
	public static final GeneralPermission WRITE = new GeneralPermission("WRITE", 1 << 2, 'W'); // 4
	public static final GeneralPermission WRITEMETA = new GeneralPermission("WRITEMETA", 1 << 3, 'M'); // 8

	protected GeneralPermission(String name, int mask)
	{
		super(mask);
		this.name = name;
	}

	public GeneralPermission(String name, int mask, char code)
	{
		super(mask, code);
		this.name = name;
	}
}
