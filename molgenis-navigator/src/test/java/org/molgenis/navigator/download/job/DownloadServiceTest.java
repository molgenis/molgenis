package org.molgenis.navigator.download.job;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.export.EmxExportService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.navigator.util.ResourceCollection;
import org.molgenis.navigator.util.ResourceCollector;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DownloadServiceTest extends AbstractMolgenisSpringTest {

  @Mock EmxExportService downloadService;

  @Mock Progress progress;

  @Mock FileStore fileStore;

  @Mock FileMetaFactory fileMetaFactory;

  @Mock DataService dataService;

  @Mock ResourceCollector resourceCollector;

  @Mock MessageSource messageSource;

  @BeforeMethod
  public void messageTestBeforeMethod() {
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @Test
  public void testDownload() {
    doReturn("done")
        .when(messageSource)
        .getMessage(
            "progress-download-success",
            new Object[] {},
            "Finished preparing download.",
            LocaleContextHolder.getLocale());

    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMetaFactory.create(anyString())).thenReturn(fileMeta);
    File file = mock(File.class);
    when(file.getName()).thenReturn("test");
    when(fileStore.getFile(anyString())).thenReturn(file);

    ResourceIdentifier id1 = ResourceIdentifier.create(ResourceType.PACKAGE, "it");
    ResourceIdentifier id2 = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, "test_entity");
    ResourceCollection collection = mock(ResourceCollection.class);
    when(resourceCollector.get(newArrayList(id1, id2))).thenReturn(collection);
    EntityType entityType1 = mock(EntityType.class);
    Package package1 = mock(Package.class);
    when(collection.getEntityTypes()).thenReturn(newArrayList(entityType1));
    when(collection.getPackages()).thenReturn(newArrayList(package1));

    ResourceDownloadService downloadJob =
        new ResourceDownloadService(
            downloadService, fileStore, fileMetaFactory, dataService, resourceCollector);
    List<ResourceIdentifier> resourceIdentifierList =
        newArrayList(
            ResourceIdentifier.create(ResourceType.PACKAGE, "it"),
            ResourceIdentifier.create(ResourceType.ENTITY_TYPE, "test_entity"));
    downloadJob.download(resourceIdentifierList, "test", progress);
    verify(downloadService)
        .export(newArrayList(entityType1), newArrayList(package1), file.toPath(), progress);
    verify(progress).increment(1);
    verify(progress).status("done");
  }
}
