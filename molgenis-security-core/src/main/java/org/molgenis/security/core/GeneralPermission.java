package org.molgenis.security.core;

import org.springframework.security.acls.domain.AbstractPermission;

//FIXME: rename ang move to proper module
public class GeneralPermission extends AbstractPermission
{
	public static final GeneralPermission READ = new GeneralPermission(1 << 0, 'R'); // 1

	protected GeneralPermission(int mask)
	{
		super(mask);
	}

	public GeneralPermission(int mask, char code)
	{
		super(mask, code);
	}
}
