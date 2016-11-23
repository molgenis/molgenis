package org.molgenis.bootstrap.populate;

import org.molgenis.data.DataService;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.file.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.DatabaseAction.ADD_IGNORE_EXISTING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 * Imports i18n static strings from molgenis-core-ui/src/main/resources/i18n.xlsx at startup.
 * <p>
 * Only adds new strings, does not update existing ones, because otherwise the ones you have changed using the
 * dataexplorer will be overwritten again on the next startup.
 */
@Component
public class I18nPopulator
{
	private static final Logger LOG = LoggerFactory.getLogger(I18nPopulator.class);

	private final DataService dataService;
	private final LanguageFactory languageFactory;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final ImportServiceFactory importServiceFactory;
	private final FileStore fileStore;

	@Autowired
	public I18nPopulator(DataService dataService, LanguageFactory languageFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, ImportServiceFactory importServiceFactory,
			FileStore fileStore)
	{
		this.dataService = requireNonNull(dataService);
		this.languageFactory = requireNonNull(languageFactory);
		this.fileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
		this.importServiceFactory = requireNonNull(importServiceFactory);
		this.fileStore = requireNonNull(fileStore);
	}

	/**
	 * Populate data store with languages and internationalization strings
	 */
	void populate()
	{
		populateLanguages();
		populateI18nStrings();
	}

	/**
	 * Populate data store with internationalization strings
	 */
	private void populateI18nStrings()
	{
		final String i18nFileName = "i18n.xlsx";

		// "i18n is saved as a Application/Library resource.
		// "It is not possible to use it as a file but should streamed as a resource"
		InputStream is = I18nPopulator.class.getClassLoader().getResourceAsStream(i18nFileName);

		try
		{
			File fileInTempDir = fileStore.store(is, i18nFileName);
			LOG.trace("Create temp file for {} : {}", i18nFileName, fileInTempDir);

			FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory
					.createFileRepositoryCollection(fileInTempDir);

			ImportService importService = importServiceFactory.getImportService(fileInTempDir, repoCollection);
			runAsSystem(() -> importService.doImport(repoCollection, ADD_IGNORE_EXISTING, PACKAGE_DEFAULT));

			if (fileInTempDir.exists())
			{
				LOG.trace("Delete temp file for {} : {}", i18nFileName, fileInTempDir);
				fileInTempDir.delete();
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Populate data store with default languages
	 */
	private void populateLanguages()
	{
		dataService.add(LANGUAGE, languageFactory
				.create(LanguageService.DEFAULT_LANGUAGE_CODE, LanguageService.DEFAULT_LANGUAGE_NAME, true));
		dataService
				.add(LANGUAGE, languageFactory.create("nl", new Locale("nl").getDisplayName(new Locale("nl")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("pt", new Locale("pt").getDisplayName(new Locale("pt")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("es", new Locale("es").getDisplayName(new Locale("es")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("de", new Locale("de").getDisplayName(new Locale("de")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("it", new Locale("it").getDisplayName(new Locale("it")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("fr", new Locale("fr").getDisplayName(new Locale("fr")), false));
		dataService.add(LANGUAGE, languageFactory.create("xx", "My language", false));
	}
}
