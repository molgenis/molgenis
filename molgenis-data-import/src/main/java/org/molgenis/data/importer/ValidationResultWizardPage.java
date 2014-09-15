package org.molgenis.data.importer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@Component
public class ValidationResultWizardPage extends AbstractWizardPage
{
	private static final Logger logger = Logger.getLogger(ValidationResultWizardPage.class);
	private static final long serialVersionUID = 1L;
	private final ExecutorService asyncImportJobs = Executors.newCachedThreadPool();

	@Autowired
	private ImportServiceFactory importServiceFactory;

	@Autowired
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private ImportRunService importRunService;

	@Autowired
	private ImportPostProcessingService importPostProcessingService;

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
				ImportService importService = importServiceFactory.getImportService(importWizard.getFile(),
						repositoryCollection);

				synchronized (this)
				{
					ImportRun importRun = importRunService.addImportRun(SecurityUtils.getCurrentUsername());
					((ImportWizard) wizard).setImportRunId(importRun.getId());

					asyncImportJobs.execute(new ImportJob(importService, SecurityContextHolder.getContext(),
							repositoryCollection, entityDbAction, importRun.getId(), importRunService,
							importPostProcessingService, request.getSession()));
				}

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

		}

		return null;
	}

	private DatabaseAction toDatabaseAction(String actionStr)
	{
		// convert input to database action
		DatabaseAction dbAction;

		if (actionStr.equals("add")) dbAction = DatabaseAction.ADD;
		else if (actionStr.equals("add_update")) dbAction = DatabaseAction.ADD_UPDATE_EXISTING;
		else if (actionStr.equals("update")) dbAction = DatabaseAction.UPDATE;
		else dbAction = null;

		return dbAction;
	}
}
