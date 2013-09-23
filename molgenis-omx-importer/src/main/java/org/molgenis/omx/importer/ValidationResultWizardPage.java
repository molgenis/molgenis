package org.molgenis.omx.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.ApplicationUtil;
import org.molgenis.util.DataSetImportedEvent;
import org.springframework.transaction.annotation.Transactional;

public class ValidationResultWizardPage extends WizardPage
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ValidationResultWizardPage.class);

	public ValidationResultWizardPage()
	{
		super("Validation");
	}

	@Override
	@Transactional
	public void handleRequest(Database db, HttpServletRequest request)
	{
		ImportWizard importWizard = getWizard();
		String entityImportOption = importWizard.getEntityImportOption();

		if (entityImportOption != null)
		{
			try
			{
				doImport(db, entityImportOption);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			catch (DatabaseException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	private void doImport(Database db, String entityAction) throws IOException, DatabaseException
	{
		ImportWizard importWizard = getWizard();

		File file = importWizard.getFile();
		try
		{
			// convert input to database action
			DatabaseAction entityDbAction = toDatabaseAction(entityAction);
			if (entityDbAction == null) throw new IOException("unknown database action: " + entityAction);

			// import entities
			EntityImportReport importReport = ApplicationUtil.getEntitiesImporter()
					.importEntities(file, entityDbAction);
			importWizard.setImportResult(importReport);

			// import dataset instances
			if (importWizard.getDataImportable() != null)
			{
				List<String> dataSetSheetNames = new ArrayList<String>();
				for (Entry<String, Boolean> entry : importWizard.getDataImportable().entrySet())
					if (entry.getValue() == true) dataSetSheetNames.add("dataset_" + entry.getKey());

				ApplicationContextProvider.getApplicationContext().getBean(DataSetImporterService.class)
						.importDataSet(file, dataSetSheetNames);
			}

			importWizard.setSuccessMessage("File successfully imported.");
		}
		catch (IOException e)
		{
			logger.warn("Import of file [" + file.getName() + "] failed for action [" + entityAction + "]", e);
			importWizard.setValidationMessage("<b>Your import failed:</b><br />" + e.getMessage());
			throw e;
		}
		catch (DatabaseException e)
		{
			logger.warn("Import of file [" + file.getName() + "] failed for action [" + entityAction + "]", e);
			importWizard.setValidationMessage("<b>Your import failed:</b><br />" + e.getMessage());
			throw e;
		}

		// publish dataset imported event(s)
		try
		{
			for (DataSet dataSet : db.find(DataSet.class))
				ApplicationContextProvider.getApplicationContext().publishEvent(
						new DataSetImportedEvent(this, dataSet.getId()));
		}
		catch (DatabaseException e)
		{
			logger.error("Error publishing " + DataSet.class.getSimpleName() + " imported event(s)");
			throw e;
		}
	}

	private DatabaseAction toDatabaseAction(String actionStr)
	{
		// convert input to database action
		DatabaseAction dbAction;

		if (actionStr.equals("add")) dbAction = DatabaseAction.ADD;
		else if (actionStr.equals("add_ignore")) dbAction = DatabaseAction.ADD_IGNORE_EXISTING;
		else if (actionStr.equals("add_update")) dbAction = DatabaseAction.ADD_UPDATE_EXISTING;
		else if (actionStr.equals("update")) dbAction = DatabaseAction.UPDATE;
		else if (actionStr.equals("update_ignore")) dbAction = DatabaseAction.UPDATE_IGNORE_MISSING;
		else dbAction = null;

		return dbAction;
	}

}
