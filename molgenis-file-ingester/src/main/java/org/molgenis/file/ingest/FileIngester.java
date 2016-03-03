package org.molgenis.file.ingest;

import java.io.File;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.framework.db.EntityImportReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Imports a file from a remote url.
 * 
 * For now only csv files are supported
 */
@Component
public class FileIngester
{
	private static final Logger LOG = LoggerFactory.getLogger(FileIngester.class);
	private final FileStoreDownload fileStoreDownload;
	private final ImportServiceFactory importServiceFactory;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final FileIngesterLoggerFactory fileIngesterLoggerFactory;

	@Autowired
	public FileIngester(FileStoreDownload fileStoreDownload, ImportServiceFactory importServiceFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			FileIngesterLoggerFactory fileIngesterLoggerFactory)
	{
		this.fileStoreDownload = fileStoreDownload;
		this.importServiceFactory = importServiceFactory;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.fileIngesterLoggerFactory = fileIngesterLoggerFactory;
	}

	/**
	 * Imports a csv file defined in the fileIngest entity
	 * 
	 * @see FileIngestMetaData
	 * 
	 * @param fileIngest
	 */
	public void ingest(Entity fileIngest)
	{
		FileIngesterLogger logger = null;
		try
		{
			Entity entityMetaData = fileIngest.getEntity(FileIngestMetaData.ENTITY_META_DATA);
			String entityName = entityMetaData.getString(EntityMetaDataMetaData.FULL_NAME);
			String url = fileIngest.getString(FileIngestMetaData.URL);
			String loader = fileIngest.getString(FileIngestMetaData.LOADER);

			if (!"CSV".equals(loader))
			{
				throw new FileIngestException("Unknown loader '" + loader + "'");
			}

			LOG.info("Ingesting url '{}'", url);
			logger = fileIngesterLoggerFactory.createLogger();
			String jobMetaDataIdentifier = logger.start(fileIngest);

			File file = fileStoreDownload.downloadFile(url, jobMetaDataIdentifier, entityName + ".csv");
			logger.downloadFinished(file, "text/csv");

			FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory
					.createFileRepositoryCollection(file);
			ImportService importService = importServiceFactory.getImportService(file, repoCollection);
			EntityImportReport report = importService.doImport(repoCollection, DatabaseAction.ADD_UPDATE_EXISTING,
					Package.DEFAULT_PACKAGE_NAME);

			LOG.info("Ingestion of url '{}' done.", url);
			logger.success(report);
		}
		catch (Exception e)
		{
			LOG.error("Error ingesting url", e);
			if (logger != null)
			{
				logger.failure(e);
			}
		}
	}

}
