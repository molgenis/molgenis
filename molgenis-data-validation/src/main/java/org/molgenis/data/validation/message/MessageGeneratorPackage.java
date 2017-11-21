package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.validation.constraint.PackageConstraint;
import org.molgenis.data.validation.constraint.PackageValidationResult;
import org.molgenis.util.UnexpectedEnumException;

import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;
import static org.molgenis.data.validation.constraint.PackageConstraint.NAME;
import static org.molgenis.data.validation.constraint.PackageConstraint.SYSTEM_PACKAGE_READ_ONLY;

class MessageGeneratorPackage
{
	private MessageGeneratorPackage()
	{
	}

	static List<ConstraintViolationMessage> createMessages(PackageValidationResult constraintViolation)
	{
		Package aPackage = constraintViolation.getPackage();
		return constraintViolation.getConstraintViolations()
								  .stream()
								  .map(packageConstraint -> createMessage(aPackage, packageConstraint))
								  .collect(toList());
	}

	private static ConstraintViolationMessage createMessage(Package aPackage, PackageConstraint packageConstraint)
	{
		ConstraintViolationMessage constraintViolationMessage;

		switch (packageConstraint)
		{
			case SYSTEM_PACKAGE_READ_ONLY:
				constraintViolationMessage = createMessageSystemPackageReadOnly("V30", aPackage);
				break;
			case NAME:
				constraintViolationMessage = createMessageName("V31", aPackage);
				break;
			default:
				throw new UnexpectedEnumException(packageConstraint);
		}
		return constraintViolationMessage;
	}

	private static String getMessage(Package aPackage, PackageConstraint packageConstraint)
	{
		return String.format("constraint:%s package:%s", packageConstraint.name(), aPackage.getId());
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageSystemPackageReadOnly(String errorCode, Package aPackage)
	{
		String message = getMessage(aPackage, SYSTEM_PACKAGE_READ_ONLY);
		String localizedMessage = getLocalizedMessage(errorCode, aPackage.getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageName(String errorCode, Package aPackage)
	{
		String message = getMessage(aPackage, NAME);
		String localizedMessage = getLocalizedMessage(errorCode, aPackage.getLabel(), aPackage.getId()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}
}
