package org.molgenis.data.importer;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class PackageWizardPage extends AbstractWizardPage
{
	private static final Logger LOG = LoggerFactory.getLogger(PackageWizardPage.class);

	/**
	 * Auto generated
	 */
	private static final long serialVersionUID = 1L;

	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private ImportServiceFactory importServiceFactory;
	private MetaDataService metaDataService;

	@Autowired
	public PackageWizardPage(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			ImportServiceFactory importServiceFactory, MetaDataService metaDataService)
	{
		this.fileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
		this.importServiceFactory = requireNonNull(importServiceFactory);
		this.metaDataService = requireNonNull(metaDataService);
	}

	@Override
	public String getTitle()
	{
		return "Packages";
	}

	@Override
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

				RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
						.createFileRepositoryCollection(importWizard.getFile());

				ImportService importService = importServiceFactory.getImportService(importWizard.getFile(),
						repositoryCollection);

				// Do integration test only if there are no previous errors found
				if (!importWizard.getEntitiesImportable().containsValue(false))
				{
					// The package name that is selected in the "package selection" page
					String defaultPackage = request.getParameter("defaultEntity");

					LinkedHashMap<String, Boolean> entitiesImportable = importService.integrationTestMetaData(
							metaDataService, repositoryCollection, defaultPackage);

					importWizard.setEntitiesImportable(entitiesImportable);

					List<String> entitiesNotImportable = entitiesImportable.entrySet().stream()
							.filter(e -> e.getValue() == false).map(e -> e.getKey()).collect(Collectors.toList());
					if (!entitiesNotImportable.isEmpty())
					{
						throw new RuntimeException(
								"You are trying to upload entities that are not compatible with the already existing entities: "
										+ entitiesNotImportable.toString());
					}
				}
			}
			catch (RuntimeException e)
			{
				ImportWizardUtil.handleException(e, importWizard, result, LOG, entityImportOption);
			}
			catch (IOException e)
			{
				ImportWizardUtil.handleException(e, importWizard, result, LOG, entityImportOption);
			}
		}
		return null;
	}
}
