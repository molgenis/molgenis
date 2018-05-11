package org.molgenis.security.core;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.acls.domain.AbstractPermission;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Predefined set of permissions to grant in an ACE.
 */
public class PermissionSet extends AbstractPermission
{
	private String name;

	public static final int COUNT_MASK = 1;
	public static final int READ_MASK = 2;
	public static final int WRITE_MASK = 4;
	public static final int WRITEMETA_MASK = 8;

	public static final PermissionSet COUNT = new PermissionSet("Count", COUNT_MASK, 'C');
	public static final PermissionSet READ = new PermissionSet("Read", READ_MASK, 'R');
	public static final PermissionSet WRITE = new PermissionSet("Write", WRITE_MASK, 'W');
	public static final PermissionSet WRITEMETA = new PermissionSet("Manage", WRITEMETA_MASK, 'M');

	protected PermissionSet(String name, int mask)
	{
		super(mask);
		this.name = name;
	}

	public PermissionSet(String name, int mask, char code)
	{
		super(mask, code);
		this.name = name;
	}

	public MessageSourceResolvable getName()
	{
		String code = Stream.of("permission", getClass().getSimpleName(), name).collect(joining("."));
		return new DefaultMessageSourceResolvable(new String[] { code }, null, name);
	}
}
