package org.molgenis.data.importer.wizard;

import com.google.common.collect.Lists;
import org.molgenis.auth.Group;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.*;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.security.user.UserService;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.GroupMetaData.GROUP;

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
	UserAccountService userAccountService;

	@Autowired
	UserService userService;

	private List<Group> groups;

	@Override
	public String getTitle()
	{
		return "Validation";
	}

	@Override
	@Transactional
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		ImportWizardUtil.validateImportWizard(wizard);
		ImportWizard importWizard = (ImportWizard) wizard;
		String entityImportOption = importWizard.getEntityImportOption();

		if (entityImportOption != null)
		{
			try
			{
				// convert input to database action
				DatabaseAction entityDbAction = ImportWizardUtil.toDatabaseAction(entityImportOption);
				if (entityDbAction == null) throw new IOException("unknown database action: " + entityImportOption);

				RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(
						importWizard.getFile());
				ImportService importService = importServiceFactory.getImportService(importWizard.getFile(),
						repositoryCollection);

				synchronized (this)
				{
					ImportRun importRun = importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false);
					((ImportWizard) wizard).setImportRunId(importRun.getId());

					asyncImportJobs.execute(
							new ImportJob(importService, SecurityContextHolder.getContext(), repositoryCollection,
									entityDbAction, importRun.getId(), importRunService, request.getSession(),
									importWizard.getDefaultEntity()));
				}

			}
			catch (RuntimeException | IOException e)
			{
				ImportWizardUtil.handleException(e, importWizard, result, LOG, entityImportOption);
			}

		}

		// Convert to list because it's less impossible use in FreeMarker
		if (!userAccountService.getCurrentUser().isSuperuser())
		{
			String username = SecurityUtils.getCurrentUsername();
			groups = RunAsSystemProxy.runAsSystem(() -> Lists.newArrayList(userService.getUserGroups(username)));
		}
		else
		{
			groups = dataService.findAll(GROUP, Group.class).collect(toList());
		}

		((ImportWizard) wizard).setGroups(groups);

		return null;
	}
}
