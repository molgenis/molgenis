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
import org.molgenis.framework.db.EntitiesImporter;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.DataSetImportedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@Component
public class ValidationResultWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ValidationResultWizardPage.class);
	private final Database database;
	private final EntitiesImporter entitiesImporter;
	private final DataSetImporterService dataSetImporterService;

	@Autowired
	public ValidationResultWizardPage(Database database, EntitiesImporter entitiesImporter,
			DataSetImporterService dataSetImporterService)
	{
		this.database = database;
		this.entitiesImporter = entitiesImporter;
		this.dataSetImporterService = dataSetImporterService;
		if (database == null) throw new IllegalArgumentException("Database is null");
		if (entitiesImporter == null) throw new IllegalArgumentException("EntitiesImporter is null");
		if (dataSetImporterService == null) throw new IllegalArgumentException("DataSetImporterService is null");
	}

	@Override
	public String getTitle()
	{
		return "Validation";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		if (!(wizard instanceof ImportWizard))
		{
			throw new RuntimeException("Wizard must be of type '" + ImportWizard.class.getSimpleName()
					+ "' instead of '" + wizard.getClass().getSimpleName() + "'");
		}

		ImportWizard importWizard = (ImportWizard) wizard;
		String entityImportOption = importWizard.getEntityImportOption();

		if (entityImportOption != null)
		{
			try
			{
				return doImport(entityImportOption, result, importWizard);
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

		return null;
	}

	private String doImport(String entityAction, BindingResult result, ImportWizard importWizard) throws IOException,
			DatabaseException
	{

		File file = importWizard.getFile();
		try
		{
			// convert input to database action
			DatabaseAction entityDbAction = toDatabaseAction(entityAction);
			if (entityDbAction == null) throw new IOException("unknown database action: " + entityAction);

			// import entities
			EntityImportReport importReport = entitiesImporter.importEntities(file, entityDbAction);
			importWizard.setImportResult(importReport);

			// import dataset instances
			if (importWizard.getDataImportable() != null)
			{
				List<String> dataSetSheetNames = new ArrayList<String>();
				for (Entry<String, Boolean> entry : importWizard.getDataImportable().entrySet())
					if (entry.getValue() == true) dataSetSheetNames.add("dataset_" + entry.getKey());

				dataSetImporterService.importDataSet(file, dataSetSheetNames);
			}

			// publish dataset imported event(s)
			try
			{
				for (DataSet dataSet : database.find(DataSet.class))
					ApplicationContextProvider.getApplicationContext().publishEvent(
							new DataSetImportedEvent(this, dataSet.getId()));
			}
			catch (DatabaseException e)
			{
				logger.error("Error publishing " + DataSet.class.getSimpleName() + " imported event(s)");
				throw e;
			}

			return "File successfully imported.";

		}
		catch (IOException e)
		{
			logger.warn("Import of file [" + file.getName() + "] failed for action [" + entityAction + "]", e);
			result.addError(new ObjectError("wizard", "<b>Your import failed:</b><br />" + e.getMessage()));
		}
		catch (DatabaseException e)
		{
			logger.warn("Import of file [" + file.getName() + "] failed for action [" + entityAction + "]", e);
			result.addError(new ObjectError("wizard", "<b>Your import failed:</b><br />" + e.getMessage()));
		}

		return null;
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
