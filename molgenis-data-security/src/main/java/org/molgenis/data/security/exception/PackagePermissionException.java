package org.molgenis.data.security.exception;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.PackagePermission;
import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class PackagePermissionException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "P01";
	private final PackagePermission permission;
	private final transient Package pack;

	public PackagePermissionException(PackagePermission permission, Package pack)
	{
		super(ERROR_CODE);
		this.permission = requireNonNull(permission);
		this.pack = requireNonNull(pack);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new String[] { permission.getName(), pack.getLabel() };
	}
}
