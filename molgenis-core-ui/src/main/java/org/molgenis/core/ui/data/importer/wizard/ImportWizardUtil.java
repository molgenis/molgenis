package org.molgenis.core.ui.data.importer.wizard;

import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.ValidationMessage;
import org.molgenis.i18n.CodedRuntimeException;
import org.slf4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

class ImportWizardUtil
{
	private ImportWizardUtil()
	{

	}

	static DatabaseAction toDatabaseAction(String actionStr)
	{
		// convert input to database action
		DatabaseAction dbAction;

		switch (actionStr)
		{
			case "add":
				dbAction = DatabaseAction.ADD;
				break;
			case "add_update":
				dbAction = DatabaseAction.ADD_UPDATE_EXISTING;
				break;
			case "update":
				dbAction = DatabaseAction.UPDATE;
				break;
			default:
				dbAction = null;
				break;
		}

		return dbAction;
	}

	public static void handleException(CodedRuntimeException e, ImportWizard importWizard, BindingResult result,
			Logger logger, String entityImportOption)
	{
		File file = importWizard.getFile();

		if (logger.isWarnEnabled())
		{
			logger.warn(format("Import of file [%s] failed for action [%s]",
					Optional.ofNullable(file).map(File::getName).orElse("UNKNOWN"), entityImportOption), e);
		}

		String message;
		if (e instanceof ValidationException)
		{
			ValidationException validationException = (ValidationException) e;
			message = validationException.getValidationMessages()
										 .map(ValidationMessage::getLocalizedMessage)
										 .collect(Collectors.joining("<br />"));
		}
		else
		{
			message = e.getLocalizedMessage();
			message = getCauseMessage(e.getCause(), message);
		}
		result.addError(new ObjectError("wizard", "<b>Your import failed:</b><br />" + message));
	}

	private static String getCauseMessage(Throwable e, String message)
	{
		if (e instanceof CodedRuntimeException)
		{
			message += "<br />" + e.getLocalizedMessage();
			getCauseMessage(e.getCause(), message);
		}
		return message;
	}

	static void validateImportWizard(Wizard wizard)
	{
		if (!(wizard instanceof ImportWizard))
		{
			throw new RuntimeException(
					"Wizard must be of type '" + ImportWizard.class.getSimpleName() + "' instead of '"
							+ wizard.getClass().getSimpleName() + "'");
		}
	}
}
