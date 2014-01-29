package org.molgenis.omx.importer;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntitySource;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.EntityImportedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@Component
public class ValidationResultWizardPage extends AbstractWizardPage
{
	private static final Logger logger = Logger.getLogger(ValidationResultWizardPage.class);

	private static final long serialVersionUID = 1L;

	@Autowired
	private OmxImporterService omxImporterService;

	@Autowired
	private DataService dataService;

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
				// convert input to database action
				DatabaseAction entityDbAction = toDatabaseAction(entityImportOption);
				if (entityDbAction == null) throw new IOException("unknown database action: " + entityImportOption);

				EntitySource repository = dataService.createEntitySource(importWizard.getFile());
				EntityImportReport importReport = omxImporterService.doImport(repository,
						importWizard.getDataImportable(), entityDbAction);
				importWizard.setImportResult(importReport);

				// publish dataset imported event(s)
				Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME);
				for (DataSet dataSet : dataSets)
					ApplicationContextProvider.getApplicationContext().publishEvent(
							new EntityImportedEvent(this, DataSet.ENTITY_NAME, dataSet.getId()));

				// publish protocol imported event(s)
				Iterable<Protocol> protocols = dataService.findAll(Protocol.ENTITY_NAME,
						new QueryImpl().eq(Protocol.ROOT, true));
				for (Protocol protocol : protocols)
					ApplicationContextProvider.getApplicationContext().publishEvent(
							new EntityImportedEvent(this, Protocol.ENTITY_NAME, protocol.getId()));

				return "File successfully imported.";
			}
			catch (RuntimeException e)
			{
				File file = importWizard.getFile();
				logger.warn("Import of file [" + file.getName() + "] failed for action [" + entityImportOption + "]", e);
				result.addError(new ObjectError("wizard", "<b>Your import failed:</b><br />" + e.getMessage()));
			}
			catch (IOException e)
			{
				File file = importWizard.getFile();
				logger.warn("Import of file [" + file.getName() + "] failed for action [" + entityImportOption + "]", e);
				result.addError(new ObjectError("wizard", "<b>Your import failed:</b><br />" + e.getMessage()));
			}
			catch (ValueConverterException e)
			{
				File file = importWizard.getFile();
				logger.warn("Import of file [" + file.getName() + "] failed for action [" + entityImportOption + "]", e);
				result.addError(new ObjectError("wizard", "<b>Your import failed:</b><br />" + e.getMessage()));
			}
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
