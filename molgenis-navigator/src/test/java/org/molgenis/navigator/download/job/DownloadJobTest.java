package org.molgenis.navigator.download.job;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.export.EmxExportService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.jobs.Progress;
import org.testng.annotations.Test;

public class DownloadJobTest extends AbstractMolgenisSpringTest {

  @Mock EmxExportService downloadService;

  @Mock Progress progress;

  @Mock FileStore fileStore;

  @Mock FileMetaFactory fileMetaFactory;

  @Mock DataService dataService;

  @Test
  public void testDownload() {
    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMetaFactory.create(anyString())).thenReturn(fileMeta);
    File file = mock(File.class);
    when(file.getName()).thenReturn("test");
    when(fileStore.getFile(anyString())).thenReturn(file);
    DownloadService downloadJob =
        new DownloadService(downloadService, fileStore, fileMetaFactory, dataService);
    String json = "[{'id':'it','type':'PACKAGE'},{'id':'test_entity','type':'ENTITY_TYPE'}]";
    downloadJob.download(json, "test", progress);
    verify(progress).progress(0, "Starting download...");
    verify(downloadService).download(newArrayList("test_entity"), newArrayList("it"), file);
    verify(progress).progress(1, "Download finished");
  }
}
