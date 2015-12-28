package org.molgenis.ui;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.io.File;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.support.FileRepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.google.common.io.Resources;

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

	@Autowired
	public I18nStringsPopulator(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			ImportServiceFactory importServiceFactory)
	{
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importServiceFactory = importServiceFactory;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		LOG.info("Importing i18n strings...");

		File file = new File(Resources.getResource("i18n.xlsx").getPath());
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);

		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		runAsSystem(() -> importService.doImport(repoCollection, DatabaseAction.ADD_IGNORE_EXISTING,
				Package.DEFAULT_PACKAGE_NAME));

		LOG.info("Importing i18n strings done");
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}
}
