package org.molgenis.file.ingest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.MapEntity;
import org.molgenis.file.ingest.execution.FileIngester;
import org.molgenis.file.ingest.execution.FileStoreDownload;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.framework.db.EntityImportReport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileIngesterTest
{
	private FileIngester fileIngester;

	private FileStoreDownload fileStoreDownloadMock;
	private ImportServiceFactory importServiceFactoryMock;
	private ImportService importServiceMock;
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactoryMock;
	private FileRepositoryCollection fileRepositoryCollectionMock;

	private final String entityName = "test";
	private final String url = "http://www.test.nl/test";
	private final String identifier = "identifier";
	private final File f = new File("");
	private final EntityImportReport report = new EntityImportReport();
	private Entity entityMetaData;
	private Entity fileIngest;

	private DataService dataService;
	private Progress progress;

	@BeforeMethod
	public void setUp()
	{
		fileStoreDownloadMock = mock(FileStoreDownload.class);
		fileRepositoryCollectionFactoryMock = mock(FileRepositoryCollectionFactory.class);
		fileRepositoryCollectionMock = mock(FileRepositoryCollection.class);
		importServiceFactoryMock = mock(ImportServiceFactory.class);
		importServiceMock = mock(ImportService.class);
		dataService = mock(DataService.class);
		progress = mock(Progress.class);

		fileIngester = new FileIngester(fileStoreDownloadMock, importServiceFactoryMock,
				fileRepositoryCollectionFactoryMock, dataService);

		entityMetaData = new MapEntity(EntityMetaDataMetaData.FULL_NAME, entityName);
		fileIngest = new MapEntity();
		fileIngest.set(FileIngestMetaData.ENTITY_META_DATA, entityMetaData);
		fileIngest.set(FileIngestMetaData.URL, url);
		fileIngest.set(FileIngestMetaData.LOADER, "CSV");
	}

	@Test
	public void ingest()
	{
		when(fileStoreDownloadMock.downloadFile(url, identifier, entityName + ".csv")).thenReturn(f);
		when(fileRepositoryCollectionFactoryMock.createFileRepositoryCollection(f))
				.thenReturn(fileRepositoryCollectionMock);
		when(importServiceFactoryMock.getImportService(f, fileRepositoryCollectionMock)).thenReturn(importServiceMock);
		when(importServiceMock.doImport(fileRepositoryCollectionMock, DatabaseAction.ADD_UPDATE_EXISTING,
				Package.DEFAULT_PACKAGE_NAME)).thenReturn(report);

		fileIngester.ingest(entityName, url, "CSV", identifier, progress, "a@b.com,x@y.com");

	}

	@Test(expectedExceptions = RuntimeException.class)
	public void ingestError()
	{
		Exception e = new RuntimeException();
		when(fileStoreDownloadMock.downloadFile(url, identifier, entityName + ".csv")).thenThrow(e);

		fileIngester.ingest(entityName, url, "CSV", identifier, progress, "a@b.com,x@y.com");
	}
}
