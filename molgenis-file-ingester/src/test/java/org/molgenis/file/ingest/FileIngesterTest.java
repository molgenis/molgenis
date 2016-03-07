package org.molgenis.file.ingest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.MapEntity;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.framework.db.EntityImportReport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileIngesterTest {
	private FileIngester fileIngester;

	private FileStoreDownload fileStoreDownloadMock;
	private ImportServiceFactory importServiceFactoryMock;
	private ImportService importServiceMock;
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactoryMock;
	private FileRepositoryCollection fileRepositoryCollectionMock;
	private FileIngesterLoggerFactory fileIngesterLoggerFactoryMock;
	private FileIngesterLogger fileIngesterLoggerMock;

	private final String entityName = "test";
	private final String url = "http://www.test.nl/test";
	private final String identifier = "identifier";
	private final File f = new File("");
	private final EntityImportReport report = new EntityImportReport();
	private Entity entityMetaData;
	private Entity fileIngest;

	@BeforeMethod
	public void setUp()
	{
		fileStoreDownloadMock = mock(FileStoreDownload.class);
		fileRepositoryCollectionFactoryMock = mock(FileRepositoryCollectionFactory.class);
		fileRepositoryCollectionMock = mock(FileRepositoryCollection.class);
		importServiceFactoryMock = mock(ImportServiceFactory.class);
		importServiceMock = mock(ImportService.class);
		fileIngesterLoggerFactoryMock = mock(FileIngesterLoggerFactory.class);
		fileIngesterLoggerMock = mock(FileIngesterLogger.class);

		fileIngester = new FileIngester(fileStoreDownloadMock, importServiceFactoryMock,
				fileRepositoryCollectionFactoryMock, fileIngesterLoggerFactoryMock);

		entityMetaData = new MapEntity(EntityMetaDataMetaData.FULL_NAME, entityName);
		fileIngest = new MapEntity();
		fileIngest.set(FileIngestMetaData.ENTITY_META_DATA, entityMetaData);
		fileIngest.set(FileIngestMetaData.URL, url);
		fileIngest.set(FileIngestMetaData.LOADER, "CSV");
	}

	@Test
	public void ingest()
	{
		when(fileIngesterLoggerFactoryMock.createLogger()).thenReturn(fileIngesterLoggerMock);
		when(fileIngesterLoggerMock.start(fileIngest)).thenReturn(identifier);
		when(fileStoreDownloadMock.downloadFile(url, identifier, entityName + ".csv")).thenReturn(f);
		when(fileRepositoryCollectionFactoryMock.createFileRepositoryCollection(f)).thenReturn(
				fileRepositoryCollectionMock);
		when(importServiceFactoryMock.getImportService(f, fileRepositoryCollectionMock)).thenReturn(importServiceMock);
		when(
				importServiceMock.doImport(fileRepositoryCollectionMock, DatabaseAction.ADD_UPDATE_EXISTING,
						Package.DEFAULT_PACKAGE_NAME)).thenReturn(report);

		fileIngester.ingest(fileIngest);

		verify(fileIngesterLoggerMock).downloadFinished(f, "text/csv");
		verify(fileIngesterLoggerMock).success(report);
	}

	@Test
	public void ingestError()
	{
		Exception e = new RuntimeException();
		when(fileIngesterLoggerFactoryMock.createLogger()).thenReturn(fileIngesterLoggerMock);
		when(fileIngesterLoggerMock.start(fileIngest)).thenReturn(identifier);
		when(fileStoreDownloadMock.downloadFile(url, identifier, entityName + ".csv")).thenThrow(e);

		fileIngester.ingest(fileIngest);

		verify(fileIngesterLoggerMock).failure(e);
	}
}
