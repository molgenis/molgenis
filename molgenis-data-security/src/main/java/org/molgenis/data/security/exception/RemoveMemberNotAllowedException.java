package org.molgenis.data.security.exception;

import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.User;
import org.molgenis.i18n.CodedRuntimeException;

public class RemoveMemberNotAllowedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "DS12";

	private final transient User currentUser;
	private final transient Group group;

	public RemoveMemberNotAllowedException(User currentUser, Group group)
	{
		super(ERROR_CODE);
		this.currentUser = currentUser;
		this.group = group;
	}

	@Override
	public String getMessage()
	{
		return String.format("user:%s group:%s", currentUser.getUsername(), group.getName());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { currentUser.getUsername(), group.getName() };
	}
}
