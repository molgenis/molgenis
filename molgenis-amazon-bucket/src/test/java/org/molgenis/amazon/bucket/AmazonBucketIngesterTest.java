package org.molgenis.amazon.bucket;

import com.amazonaws.services.s3.AmazonS3Client;
import org.molgenis.amazon.bucket.client.AmazonBucketClient;
import org.molgenis.amazon.bucket.config.AmazonBucketTestConfig;
import org.molgenis.amazon.bucket.meta.AmazonBucketJobExecution;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.security.auth.SecurityPackage;
import org.molgenis.jobs.Progress;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.molgenis.data.DatabaseAction.ADD_UPDATE_EXISTING;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;

@ContextConfiguration(classes = { AmazonBucketIngesterTest.Config.class })
public class AmazonBucketIngesterTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private AmazonBucketIngester amazonBucketIngester;

	@Autowired
	private ImportServiceFactory importServiceFactoryMock;

	@Autowired
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactoryMock;

	private FileRepositoryCollection fileRepositoryCollectionMock;

	private final File f = new File("");
	private EntityImportReport report;

	private Progress progress;
	private ImportService importServiceMock;

	@BeforeTest
	public void setUp()
	{
		fileRepositoryCollectionMock = mock(FileRepositoryCollection.class);
		progress = mock(Progress.class);
		importServiceMock = mock(ImportService.class);
		report = mock(EntityImportReport.class);
	}

	@Test
	public void ingest() throws FileNotFoundException
	{
		Map<String, Integer> imported = new HashMap<>();
		imported.put("test", 1);
		when(report.getNrImportedEntitiesMap()).thenReturn(imported);
		when(fileRepositoryCollectionFactoryMock.createFileRepositoryCollection(f)).thenReturn(
				fileRepositoryCollectionMock);
		when(importServiceFactoryMock.getImportService("test_data_only.xlsx")).thenReturn(importServiceMock);
		when(importServiceMock.doImport(any(), eq(ADD_UPDATE_EXISTING), eq(PACKAGE_DEFAULT))).thenReturn(report);
		when(progress.getJobExecution()).thenReturn(mock(AmazonBucketJobExecution.class));

		amazonBucketIngester.ingest("jobExecutionID", "targetEntityTypeName", "bucket", "key(.*)", null, "test", "test",
				"region1", true, progress);
		verify(importServiceFactoryMock).getImportService("test_data_only.xlsx");
		verify(importServiceMock).doImport(any(), eq(ADD_UPDATE_EXISTING), eq(PACKAGE_DEFAULT));
	}

	@Configuration
	@Import({ AmazonBucketTestConfig.class })
	public static class Config
	{
		@Bean
		public AmazonBucketIngester ingester()
		{
			return new AmazonBucketIngester(importServiceFactory(), fileRepositoryCollectionFactory(),
					fileMetaFactory(), fileStore(), amazonBucketClient());
		}

		@Bean
		public FileStore fileStore()
		{
			return mock(FileStore.class);
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

		@Bean
		public AmazonBucketClient amazonBucketClient()
		{

			AmazonS3Client client = mock(AmazonS3Client.class);

			AmazonBucketClient amazonBucketClient = mock(AmazonBucketClient.class);
				File file = ResourceUtils.getFile(getClass(), "/test_data_only.xlsx");
				when(amazonBucketClient.getClient("test", "test", "region1")).thenReturn(client);
			try
			{
				when(amazonBucketClient.downloadFile(any(), any(), eq("jobExecutionID"), eq("bucket"), eq("key(.*)"),
						any(), eq(true), any())).thenReturn(file);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			return amazonBucketClient;
		}

		@Bean
		public SecurityPackage securityPackage()
		{
			return mock(SecurityPackage.class);
		}
	}
}
