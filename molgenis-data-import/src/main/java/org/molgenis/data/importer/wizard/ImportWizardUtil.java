package org.molgenis.data.importer.wizard;

import org.molgenis.data.DatabaseAction;
import org.molgenis.ui.wizard.Wizard;
import org.slf4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.io.File;

public class ImportWizardUtil
{
	public static DatabaseAction toDatabaseAction(String actionStr)
	{
		// convert input to database action
		DatabaseAction dbAction;

		if (actionStr.equals("add")) dbAction = DatabaseAction.ADD;
		else if (actionStr.equals("add_update")) dbAction = DatabaseAction.ADD_UPDATE_EXISTING;
		else if (actionStr.equals("update")) dbAction = DatabaseAction.UPDATE;
		else dbAction = null;

		return dbAction;
	}

	public static void handleException(Exception e, ImportWizard importWizard, BindingResult result, Logger LOG,
			String entityImportOption)
	{
		File file = importWizard.getFile();

		LOG.warn("Import of file [" + (file != null ? file.getName() : "UNKNOWN") + "] failed for action ["
				+ entityImportOption + "]", e);

		result.addError(new ObjectError("wizard", "<b>Your import failed:</b><br />" + e.getMessage()));
	}

	public static void validateImportWizard(Wizard wizard)
	{
		if (!(wizard instanceof ImportWizard))
		{
			throw new RuntimeException(
					"Wizard must be of type '" + ImportWizard.class.getSimpleName() + "' instead of '" + wizard
							.getClass().getSimpleName() + "'");
		}
	}
}
