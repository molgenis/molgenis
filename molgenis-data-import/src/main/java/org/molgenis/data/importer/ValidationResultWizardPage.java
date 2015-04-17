package org.molgenis.data.importer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import com.google.common.collect.Lists;

@Component
public class ValidationResultWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ValidationResultWizardPage.class);

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

	@Autowired
	UserAccountService userAccountService;
	private List<MolgenisGroup> groups;

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
							importPostProcessingService, request.getSession(), importWizard.getDefaultEntity()));
				}

			}
			catch (RuntimeException e)
			{
				File file = importWizard.getFile();
				LOG.warn("Import of file [" + file.getName() + "] failed for action [" + entityImportOption + "]", e);
				result.addError(new ObjectError("wizard", "<b>Your import failed:</b><br />" + e.getMessage()));
			}
			catch (IOException e)
			{
				File file = importWizard.getFile();
				LOG.warn("Import of file [" + file.getName() + "] failed for action [" + entityImportOption + "]", e);
				result.addError(new ObjectError("wizard", "<b>Your import failed:</b><br />" + e.getMessage()));
			}

		}

		// Convert to list because it's less impossible use in FreeMarker
		if (!userAccountService.getCurrentUser().getSuperuser())
		{
			groups = Lists.newArrayList(userAccountService.getCurrentUserGroups());
		}
		else
		{
			groups = Lists.newArrayList(dataService.findAll(MolgenisGroup.ENTITY_NAME, MolgenisGroup.class));
		}

		((ImportWizard) wizard).setGroups(groups);
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
