package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.validation.constraint.PackageConstraint;
import org.molgenis.data.validation.constraint.PackageConstraintViolation;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Optional;

import static java.text.MessageFormat.format;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

class MessageGeneratorPackage
{
	private MessageGeneratorPackage()
	{
	}

	static ConstraintViolationMessage createMessage(PackageConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage constraintViolationMessage;

		PackageConstraint packageConstraint = constraintViolation.getConstraint();
		switch (packageConstraint)
		{
			case SYSTEM_PACKAGE_READ_ONLY:
				constraintViolationMessage = createMessageSystemPackageReadOnly("V30", constraintViolation);
				break;
			default:
				throw new UnexpectedEnumException(packageConstraint);
		}
		return constraintViolationMessage;
	}

	private static String getMessage(PackageConstraintViolation constraintViolation)
	{
		return String.format("constraint:%s package:%s", constraintViolation.getConstraint().name(),
				constraintViolation.getPackage().getId());
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageSystemPackageReadOnly(String errorCode,
			PackageConstraintViolation constraintViolation)
	{
		Package aPackage = constraintViolation.getPackage();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, aPackage.getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}
}
