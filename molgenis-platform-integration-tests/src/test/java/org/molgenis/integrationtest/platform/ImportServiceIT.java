package org.molgenis.integrationtest.platform;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.importer.emx.EmxImportService;
import org.molgenis.data.support.FileRepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.molgenis.data.DatabaseAction.ADD_IGNORE_EXISTING;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 * Test the Importer test cases
 */
@Transactional()
@TransactionConfiguration(defaultRollback = true)
@ContextConfiguration(classes = { PlatformITConfig.class })
public class ImportServiceIT extends AbstractTestNGSpringContextTests
{
	private final static Logger LOG = LoggerFactory.getLogger(ImportServiceIT.class);

	@Autowired
	EmxImportService emxImportService;

	@Autowired
	ImportServiceFactory importServiceFactory;

	@Autowired
	FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	// FIXME should not be necessary
	@Autowired
	ImportServiceRegistrar importServiceRegistrar;

	@BeforeClass
	public void beforeClass(){
		ContextRefreshedEvent contextRefreshedEvent = Mockito.mock(ContextRefreshedEvent.class);
		Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
		importServiceRegistrar.register(contextRefreshedEvent);
	}

	@Test
	public void test() throws URISyntaxException, IOException
	{
		final String pathname = "/xls/emx_all_datatypes.xlsx";
		ClassPathResource classPath = new ClassPathResource(pathname);
		File file = classPath.getFile();
		LOG.trace("actual location: [{}]", file);

		runAsSystem(() ->
		{
			FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
			ImportService importService = importServiceFactory.getImportService(file, repoCollection);

			EntityImportReport entityImportReport = importService
					.doImport(repoCollection, ADD_IGNORE_EXISTING, PACKAGE_DEFAULT);
		});
	}
}
