package org.molgenis.ui;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.file.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Imports i18n static strings from molgenis-core-ui/src/main/resources/i18n.xlsx at startup.
 * 
 * Only adds new strings, does not update existing ones, because otherwise the ones you have changed using the
 * dataexplorer will be overwritten again on the next startup.
 */
@Component
public class I18nStringsPopulator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(I18nStringsPopulator.class);

	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final ImportServiceFactory importServiceFactory;
	private final FileStore fileStore;

	@Autowired
	public I18nStringsPopulator(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			ImportServiceFactory importServiceFactory, FileStore fileStore)
	{
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importServiceFactory = importServiceFactory;
		this.fileStore = fileStore;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		LOG.info("Importing i18n strings...");
		final String i18nFileName = "i18n.xlsx";

		// "i18n is saved as a Application/Library resource.
		// "It is not possible to use it as a file but should streamed as a resource"
		InputStream is = I18nStringsPopulator.class.getClassLoader().getResourceAsStream(i18nFileName);

		try
		{
			File fileInTempDir = fileStore.store(is, i18nFileName);
			LOG.info("Create temp file for {} : {}", i18nFileName, fileInTempDir);
			
			FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory
					.createFileRepositoryCollection(fileInTempDir);

			ImportService importService = importServiceFactory.getImportService(fileInTempDir, repoCollection);
			runAsSystem(() -> importService.doImport(repoCollection, DatabaseAction.ADD_IGNORE_EXISTING,
					Package.DEFAULT_PACKAGE_NAME));

			if (fileInTempDir.exists())
			{
				LOG.info("Delete temp file for {} : {}", i18nFileName, fileInTempDir);
				fileInTempDir.delete();
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		LOG.info("Importing i18n strings done");
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}
}
