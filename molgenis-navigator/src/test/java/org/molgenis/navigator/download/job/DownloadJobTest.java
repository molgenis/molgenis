package org.molgenis.navigator.download.job;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Optional;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.export.EmxExportService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.jobs.Progress;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DownloadJobTest extends AbstractMolgenisSpringTest {

  @Mock EmxExportService downloadService;

  @Mock Progress progress;

  @Mock FileStore fileStore;

  @Mock FileMetaFactory fileMetaFactory;

  @Mock DataService dataService;

  @Mock MessageSource messageSource;

  @BeforeMethod
  public void exceptionMessageTestBeforeMethod() {
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @Test
  public void testDownload() {
    doReturn("start")
        .when(messageSource)
        .getMessage(
            "navigator_download_start_message",
            new Object[] {},
            "Starting downloading.",
            LocaleContextHolder.getLocale());
    doReturn("done")
        .when(messageSource)
        .getMessage(
            "navigator_download_finished_message",
            new Object[] {},
            "Finished downloading.",
            LocaleContextHolder.getLocale());

    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMetaFactory.create(anyString())).thenReturn(fileMeta);
    File file = mock(File.class);
    when(file.getName()).thenReturn("test");
    when(fileStore.getFile(anyString())).thenReturn(file);
    DownloadService downloadJob =
        new DownloadService(downloadService, fileStore, fileMetaFactory, dataService);
    String json = "[{'id':'it','type':'PACKAGE'},{'id':'test_entity','type':'ENTITY_TYPE'}]";
    downloadJob.download(json, "test", progress);
    verify(progress).progress(0, "start");
    verify(downloadService)
        .download(newArrayList("test_entity"), newArrayList("it"), file, Optional.of(progress));
    verify(progress).increment(1);
    verify(progress).status("done");
  }
}
