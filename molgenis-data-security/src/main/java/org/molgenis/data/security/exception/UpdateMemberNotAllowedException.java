package org.molgenis.data.security.exception;

import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.User;
import org.molgenis.i18n.CodedRuntimeException;

public class UpdateMemberNotAllowedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "DS13";

	private final transient User currentUser;
	private final transient User member;
	private final transient Group group;

	public UpdateMemberNotAllowedException(User currentUser, User member, Group group)
	{
		super(ERROR_CODE);
		this.currentUser = currentUser;
		this.member = member;
		this.group = group;
	}

	@Override
	public String getMessage()
	{
		return String.format("currentUser:%s member:%s group:%s", currentUser.getUsername(), member.getUsername(), group.getName());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { currentUser.getUsername(), member.getUsername(), group.getName() };
	}
}
