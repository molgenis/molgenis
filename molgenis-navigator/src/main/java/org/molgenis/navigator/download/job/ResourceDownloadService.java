package org.molgenis.navigator.download.job;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.file.FileDownloadController.URI;

import java.io.File;
import java.util.List;
import org.molgenis.data.DataService;
import org.molgenis.data.export.EmxExportService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.download.exception.DownloadFailedException;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.util.ResourceCollection;
import org.molgenis.navigator.util.ResourceCollector;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ResourceDownloadService {
  private final EmxExportService emxExportService;
  private final FileStore fileStore;
  private final FileMetaFactory fileMetaFactory;
  private final DataService dataService;
  private final ResourceCollector resourceCollector;

  ResourceDownloadService(
      EmxExportService emxExportService,
      FileStore fileStore,
      FileMetaFactory fileMetaFactory,
      DataService dataService,
      ResourceCollector resourceCollector) {
    this.emxExportService = requireNonNull(emxExportService);
    this.fileStore = requireNonNull(fileStore);
    this.fileMetaFactory = requireNonNull(fileMetaFactory);
    this.dataService = requireNonNull(dataService);
    this.resourceCollector = requireNonNull(resourceCollector);
  }

  public FileMeta download(
      List<ResourceIdentifier> resourceIdentifiers, String filename, Progress progress) {
    FileMeta fileMeta;
    try {
      ResourceCollection resourceCollection = resourceCollector.get(resourceIdentifiers);
      File emxFile = fileStore.getFileUnchecked(filename);
      fileMeta = createFileMeta(emxFile);
      dataService.add(FileMetaMetaData.FILE_META, fileMeta);
      emxExportService.export(
          resourceCollection.getEntityTypes(),
          resourceCollection.getPackages(),
          emxFile.toPath(),
          progress);
      progress.increment(1);
      progress.status(getMessage("progress-download-success", "Finished preparing download."));
    } catch (RuntimeException exception) {
      throw new DownloadFailedException(exception);
    }
    return fileMeta;
  }

  private String getMessage(String key, String defaultMessage) {
    return MessageSourceHolder.getMessageSource()
        .getMessage(key, new Object[] {}, defaultMessage, LocaleContextHolder.getLocale());
  }

  private FileMeta createFileMeta(File file) {
    FileMeta fileMeta = fileMetaFactory.create(file.getName());
    fileMeta.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    fileMeta.setSize(file.length());
    fileMeta.setFilename(file.getName());
    fileMeta.setUrl(URI + "/" + file.getName());
    return fileMeta;
  }
}
