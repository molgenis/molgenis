package org.molgenis.integrationtest.sorta.controller;

import org.molgenis.auth.TokenMetaData;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.importer.OntologyImportService;
import org.molgenis.ontology.importer.repository.OntologyRepositoryCollection;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.io.File;

import static org.molgenis.integrationtest.utils.config.SecurityITConfig.SUPERUSER_NAME;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

@Component
public class SortaITUtils
{
	private final static Logger LOG = LoggerFactory.getLogger(SortaITUtils.class);

	@Autowired
	private AutowireCapableBeanFactory autowireCapableBeanFactory;

	@Autowired
	private OntologyImportService ontologyImportService;

	@Autowired
	private UserFactory userFactory;
	@Autowired
	private DataService dataService;

	public void addOntologies()
	{
		runAsSystem(() ->
		{
			try
			{
				File file = ResourceUtils.getFile(SortaControllerIT.class, "/owl/biobank_ontology.owl.zip");
				OntologyRepositoryCollection ontologyRepositoryCollection = new OntologyRepositoryCollection(file);
				autowireCapableBeanFactory.autowireBeanProperties(ontologyRepositoryCollection,
						AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
				ontologyRepositoryCollection.init();
				ontologyImportService.doImport(ontologyRepositoryCollection, DatabaseAction.ADD, "ontologiesTest");

			}
			catch (Exception err)
			{
				LOG.error("Error occurred during specific before method. ", err);
			}
		});
	}

	public void addUserIfExists(String userName)
	{
		User existingUser = dataService.getRepository(UserMetaData.USER, User.class)
									   .query()
									   .eq(UserMetaData.USERNAME, SUPERUSER_NAME)
									   .findOne();
		if (existingUser == null)
		{
			User user = userFactory.create();
			user.setUsername(userName);
			user.setPassword(userName);
			user.setEmail("admin@molgenis.org");
			dataService.add(UserMetaData.USER, user);
		}
	}

	public void cleanUp()
	{
		runAsSystem(() ->
		{
			dataService.deleteAll(TokenMetaData.TOKEN);
			dataService.deleteAll(SortaJobExecutionMetaData.SORTA_JOB_EXECUTION);
			dataService.deleteAll(OntologyTermMetaData.ONTOLOGY_TERM);
			dataService.deleteAll(OntologyMetaData.ONTOLOGY);

			User user = dataService.getRepository(UserMetaData.USER, User.class)
								   .query()
								   .eq(UserMetaData.USERNAME, SUPERUSER_NAME)
								   .findOne();
			dataService.delete(UserMetaData.USER, user);
		});
	}
}
