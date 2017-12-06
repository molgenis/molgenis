package org.molgenis.data.security.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.security.core.Permission;
import org.molgenis.util.UnexpectedEnumException;

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

	static String getPermissionName(LanguageService languageService, Permission permission)
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
		return languageService.getString("permission_" + messageKeyPostfix);
	}
}
