package org.molgenis.file.ingest;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.config.UserTestConfig;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.file.ingest.config.FileIngestTestConfig;
import org.molgenis.file.ingest.execution.FileIngester;
import org.molgenis.file.ingest.execution.FileStoreDownload;
import org.molgenis.file.ingest.meta.FileIngest;
import org.molgenis.file.ingest.meta.FileIngestFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.molgenis.data.DatabaseAction.ADD_UPDATE_EXISTING;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;

@ContextConfiguration(classes = { FileIngesterTest.Config.class })
public class FileIngesterTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private FileIngester fileIngester;

	@Autowired
	private FileStoreDownload fileStoreDownloadMock;

	@Autowired
	private ImportServiceFactory importServiceFactoryMock;

	@Autowired
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactoryMock;

	@Autowired
	private FileIngestFactory fileIngestFactory;

	@Autowired
	private DataService dataService;

	private ImportService importServiceMock;
	private FileRepositoryCollection fileRepositoryCollectionMock;

	private static final String entityName = "test";
	private static final String url = "http://www.test.nl/test";
	private static final String identifier = "identifier";
	private final File f = new File("");
	private final EntityImportReport report = new EntityImportReport();

	private Progress progress;

	@BeforeMethod
	public void setUp()
	{
		fileRepositoryCollectionMock = mock(FileRepositoryCollection.class);
		importServiceMock = mock(ImportService.class);
		progress = mock(Progress.class);

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("target").getMock();
		FileIngest fileIngest = fileIngestFactory.create();
		fileIngest.set(FileIngestMetaData.ENTITY_META_DATA, entityType);
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
		when(importServiceMock.doImport(fileRepositoryCollectionMock, ADD_UPDATE_EXISTING, PACKAGE_DEFAULT))
				.thenReturn(report);
		when(progress.getJobExecution()).thenReturn(new FileIngestJobExecution(mock(Entity.class)));

		FileMeta fileMeta = fileIngester.ingest(entityName, url, "CSV", identifier, progress, "a@b.com,x@y.com");

		verify(dataService).add("sys_FileMeta", fileMeta);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void ingestError()
	{
		Exception e = new RuntimeException();
		when(fileStoreDownloadMock.downloadFile(url, identifier, entityName + ".csv")).thenThrow(e);

		fileIngester.ingest(entityName, url, "CSV", identifier, progress, "a@b.com,x@y.com");
	}

	@Configuration
	@Import({ UserTestConfig.class, FileIngestTestConfig.class })
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public FileIngester fileIngester()
		{
			return new FileIngester(fileStoreDownload(), importServiceFactory(), fileRepositoryCollectionFactory(),
					fileMetaFactory(), dataService);
		}

		@Bean
		public FileStoreDownload fileStoreDownload()
		{
			return mock(FileStoreDownload.class);
		}

		@Bean
		public ImportServiceFactory importServiceFactory()
		{
			return mock(ImportServiceFactory.class);
		}

		@Bean
		public FileRepositoryCollectionFactory fileRepositoryCollectionFactory()
		{
			return mock(FileRepositoryCollectionFactory.class);
		}

		@Bean
		public FileMetaFactory fileMetaFactory()
		{
			FileMetaFactory fileMetaFactory = mock(FileMetaFactory.class);
			when(fileMetaFactory.create(anyString())).thenReturn(mock(FileMeta.class));
			return fileMetaFactory;
		}
	}
}
