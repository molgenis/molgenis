package org.molgenis.data.security.exception;

import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.security.core.Permission;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public abstract class PermissionDeniedException extends CodedRuntimeException
{
	PermissionDeniedException(String errorCode)
	{
		super(errorCode);
	}

	PermissionDeniedException(String errorCode, Throwable cause)
	{
		super(errorCode, cause);
	}

	static MessageSourceResolvable getMessageSourceResolvable(Permission permission)
	{
		String messageKeyPostfix;
		switch (permission)
		{
			case READ:
				messageKeyPostfix = "read";
				break;
			case WRITE:
				messageKeyPostfix = "write";
				break;
			case COUNT:
				messageKeyPostfix = "count";
				break;
			case NONE:
				messageKeyPostfix = "none";
				break;
			case WRITEMETA:
				messageKeyPostfix = "writemeta";
				break;
			default:
				throw new UnexpectedEnumException(permission);
		}
		return new DefaultMessageSourceResolvable("permission_" + messageKeyPostfix);
	}
}
