package org.molgenis.omx.importer;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.data.*;
import org.molgenis.data.importer.EmxImporterService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
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
    private EmxImporterService emxImporterService;

	@Autowired
	private DataService dataService;

	@Autowired
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

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

				RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
						.createFileRepositoryCollection(importWizard.getFile());

                //emd based import
				if (true)
				{
                    EntityImportReport importReport = emxImporterService.doImport(repositoryCollection, entityDbAction);
                    importWizard.setImportResult(importReport);
                }
                //omx based import
				else
				{

					EntityImportReport importReport = omxImporterService.doImport(repositoryCollection, entityDbAction);
					importWizard.setImportResult(importReport);

					// publish dataset imported event(s)
					Iterable<String> entities = repositoryCollection.getEntityNames();
					for (String entityName : entities)
					{
						if (entityName.startsWith(OmxImporterService.DATASET_SHEET_PREFIX))
						{
							// Import DataSet sheet, create new OmxRepository
							String dataSetIdentifier = entityName.substring(OmxImporterService.DATASET_SHEET_PREFIX
									.length());
							DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
									new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);
							ApplicationContextProvider.getApplicationContext().publishEvent(
									new EntityImportedEvent(this, DataSet.ENTITY_NAME, dataSet.getId()));
						}
						if (Protocol.ENTITY_NAME.equalsIgnoreCase(entityName))
						{
							Repository repo = repositoryCollection.getRepositoryByEntityName("protocol");

							for (Protocol protocol : repo.iterator(Protocol.class))
							{
								if (protocol.getRoot())
								{
									Protocol rootProtocol = dataService.findOne(Protocol.ENTITY_NAME,
											new QueryImpl().eq(Protocol.IDENTIFIER, protocol.getIdentifier()),
											Protocol.class);
									ApplicationContextProvider.getApplicationContext().publishEvent(
											new EntityImportedEvent(this, Protocol.ENTITY_NAME, rootProtocol.getId()));
								}
							}
						}
					}
				}

				return "File successfully imported.";
			}
			catch (MolgenisValidationException e)
			{
				File file = importWizard.getFile();
				logger.warn("Import of file [" + file.getName() + "] failed for action [" + entityImportOption + "]", e);

				StringBuilder sb = new StringBuilder("<b>Your import failed:</b><br /><br />");
				for (ConstraintViolation violation : e.getViolations())
				{
					sb.append(violation.getMessage());

					if (violation.getImportInfo() != null)
					{
						sb.append(" ").append(violation.getImportInfo());
					}

					sb.append("<br />");

				}

				result.addError(new ObjectError("wizard", sb.toString()));
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
