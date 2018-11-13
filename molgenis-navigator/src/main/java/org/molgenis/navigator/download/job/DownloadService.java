package org.molgenis.navigator.download.job;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.file.FileDownloadController.URI;

import java.io.File;
import java.util.List;
import java.util.Optional;
import org.molgenis.data.DataService;
import org.molgenis.data.export.EmxExportService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.download.exception.DownloadFailedException;
import org.molgenis.navigator.resource.Resource;
import org.molgenis.navigator.resource.Resource.Type;
import org.molgenis.navigator.resource.ResourcesUtil;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

  private final EmxExportService emxExportService;
  private final FileStore fileStore;
  private final FileMetaFactory fileMetaFactory;
  private final DataService dataService;

  public DownloadService(
      EmxExportService emxExportService,
      FileStore fileStore,
      FileMetaFactory fileMetaFactory,
      DataService dataService) {
    this.emxExportService = requireNonNull(emxExportService);
    this.fileStore = requireNonNull(fileStore);
    this.fileMetaFactory = requireNonNull(fileMetaFactory);
    this.dataService = requireNonNull(dataService);
  }

  public FileMeta download(String resourceJson, String filename, Progress progress) {
    FileMeta fileMeta;
    List<Resource> resources;
    List<String> entityTypes = newArrayList();
    List<String> packages = newArrayList();
    try {
      resources = ResourcesUtil.getResourcesFromJson(resourceJson);
      resources.forEach(
          resource -> {
            if (resource.getType().equals(Type.ENTITY_TYPE)
                || resource.getType().equals(Type.ENTITY_TYPE_ABSTRACT)) {
              entityTypes.add(resource.getId());
            } else if (resource.getType().equals(Type.PACKAGE)) {
              packages.add(resource.getId());
            }
          });

      File emxFile = fileStore.getFile(filename);
      fileMeta = createFileMeta(emxFile);
      dataService.add(FileMetaMetaData.FILE_META, fileMeta);
      emxExportService.download(entityTypes, packages, emxFile, Optional.of(progress));
      progress.increment(1);
      progress.status(getMessage("progress-download-success", "Finished preparing download."));
    } catch (CodedRuntimeException exception) {
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
