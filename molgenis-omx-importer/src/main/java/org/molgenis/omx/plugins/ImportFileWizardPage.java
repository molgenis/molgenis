package org.molgenis.omx.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.omx.dataset.DataSetImporter;

import app.EntitiesImporterImpl;

public class ImportFileWizardPage extends WizardPage
{
	public ImportFileWizardPage()
	{
		super("Import");
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		String entityImportOption = request.getString("entity_option");

		if (entityImportOption != null)
		{
			doImport(db, entityImportOption);
		}
	}

	private void doImport(Database db, String entityAction)
	{
		ImportWizard importWizard = getWizard();

		File file = importWizard.getFile();
		try
		{
			db.beginTx();

			// convert input to database action
			DatabaseAction entityDbAction = toDatabaseAction(entityAction);
			if (entityDbAction == null) throw new IOException("unknown database action: " + entityAction);

			// import entities
			EntityImportReport importReport = new EntitiesImporterImpl(db).importEntities(file, entityDbAction);
			importWizard.setImportResult(importReport);

			// import dataset instances
			if (importWizard.getDataImportable() != null)
			{
				List<String> dataSetSheetNames = new ArrayList<String>();
				for (Entry<String, Boolean> entry : importWizard.getDataImportable().entrySet())
					if (entry.getValue() == true) dataSetSheetNames.add("dataset_" + entry.getKey());

				new DataSetImporter(db).importDataSet(file, dataSetSheetNames);
			}

			importWizard.setSuccessMessage("File successfully imported.");

			db.commitTx();
		}
		catch (Exception e)
		{
			try
			{
				db.rollbackTx();
			}
			catch (DatabaseException e1)
			{
				logger.error("Exception rolling back transaction", e1);
			}

			logger.warn("Import of file [" + file.getName() + "] failed for action [" + entityAction + "]", e);
			importWizard.setValidationMessage("<b>Your import failed:</b><br />" + e.getMessage());
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
		else
			dbAction = null;

		return dbAction;
	}

}
