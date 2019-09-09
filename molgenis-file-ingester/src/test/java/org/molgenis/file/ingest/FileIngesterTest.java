package org.molgenis.file.ingest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.DataAction.ADD_UPDATE_EXISTING;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.MetadataAction;
import org.molgenis.data.security.config.UserTestConfig;
import org.molgenis.file.ingest.config.FileIngestTestConfig;
import org.molgenis.file.ingest.execution.FileIngester;
import org.molgenis.file.ingest.execution.FileStoreDownload;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.jobs.Progress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {FileIngesterTest.Config.class})
class FileIngesterTest extends AbstractMolgenisSpringTest {
  @Autowired private FileIngester fileIngester;

  @Autowired private FileStoreDownload fileStoreDownloadMock;

  @Autowired private ImportServiceFactory importServiceFactoryMock;

  @Autowired private FileRepositoryCollectionFactory fileRepositoryCollectionFactoryMock;

  @Autowired private DataService dataService;

  private ImportService importServiceMock;
  private FileRepositoryCollection fileRepositoryCollectionMock;

  private static final String entityTypeId = "test";
  private static final String url = "http://www.test.nl/test";
  private static final String identifier = "identifier";
  private final File f = new File("");
  private final EntityImportReport report = new EntityImportReport();

  private Progress progress;

  @BeforeEach
  void setUp() {
    fileRepositoryCollectionMock = mock(FileRepositoryCollection.class);
    importServiceMock = mock(ImportService.class);
    progress = mock(Progress.class);
  }

  @Test
  void ingest() {
    when(fileStoreDownloadMock.downloadFile(url, identifier, entityTypeId + ".csv")).thenReturn(f);
    when(fileRepositoryCollectionFactoryMock.createFileRepositoryCollection(f))
        .thenReturn(fileRepositoryCollectionMock);
    when(importServiceFactoryMock.getImportService(f, fileRepositoryCollectionMock))
        .thenReturn(importServiceMock);
    when(importServiceMock.doImport(
            fileRepositoryCollectionMock, MetadataAction.UPSERT, ADD_UPDATE_EXISTING, null))
        .thenReturn(report);
    when(progress.getJobExecution()).thenReturn(mock(FileIngestJobExecution.class));

    FileMeta fileMeta = fileIngester.ingest(entityTypeId, url, "CSV", identifier, progress);

    verify(dataService).add("sys_FileMeta", fileMeta);
  }

  @Test
  void ingestError() {
    Exception e = new RuntimeException();
    when(fileStoreDownloadMock.downloadFile(url, identifier, entityTypeId + ".csv")).thenThrow(e);

    assertThrows(
        RuntimeException.class,
        () -> fileIngester.ingest(entityTypeId, url, "CSV", identifier, progress));
  }

  @Configuration
  @Import({UserTestConfig.class, FileIngestTestConfig.class})
  static class Config {
    @Autowired private DataService dataService;

    @Bean
    FileIngester fileIngester() {
      return new FileIngester(
          fileStoreDownload(),
          importServiceFactory(),
          fileRepositoryCollectionFactory(),
          fileMetaFactory(),
          dataService);
    }

    @Bean
    FileStoreDownload fileStoreDownload() {
      return mock(FileStoreDownload.class);
    }

    @Bean
    ImportServiceFactory importServiceFactory() {
      return mock(ImportServiceFactory.class);
    }

    @Bean
    FileRepositoryCollectionFactory fileRepositoryCollectionFactory() {
      return mock(FileRepositoryCollectionFactory.class);
    }

    @Bean
    FileMetaFactory fileMetaFactory() {
      FileMetaFactory fileMetaFactory = mock(FileMetaFactory.class);
      when(fileMetaFactory.create(anyString())).thenReturn(mock(FileMeta.class));
      return fileMetaFactory;
    }
  }
}
