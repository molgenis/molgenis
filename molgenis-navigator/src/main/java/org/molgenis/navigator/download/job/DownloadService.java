package org.molgenis.navigator.download.job;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.file.FileDownloadController.URI;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.molgenis.data.DataService;
import org.molgenis.data.export.EmxExportService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.resource.Resource;
import org.molgenis.navigator.resource.Resource.Type;
import org.molgenis.navigator.resource.ResourcesUtil;
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
    progress.setProgressMax(1);
    progress.progress(0, "Starting download...");

    File emxFile = fileStore.getFile(filename);
    FileMeta fileMeta = createFileMeta(emxFile);
    dataService.add(FileMetaMetaData.FILE_META, fileMeta);
    List<Resource> resources;
    List<String> entityTypes = newArrayList();
    List<String> packages = newArrayList();
    resources = ResourcesUtil.getResourcesFromJson(resourceJson);
    resources.forEach(
        resource -> {
          if (resource.getType().equals(Type.ENTITY_TYPE)) {
            entityTypes.add(resource.getId());
          } else if (resource.getType().equals(Type.PACKAGE)) {
            packages.add(resource.getId());
          }
        });

    emxExportService.download(entityTypes, packages, emxFile);
    progress.progress(1, "Download finished");
    return fileMeta;
  }

  private FileMeta createFileMeta(File file) {
    FileMeta fileMeta = fileMetaFactory.create(file.getName());
    fileMeta.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    fileMeta.setSize(file.length());
    fileMeta.setFilename(file.getName());
    fileMeta.setUrl(URI + "/" + file.getName());
    return fileMeta;
  }

  public String getDownloadFilename(String extension) {
    String timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss"));
    return String.format("%s.%s", timestamp, extension);
  }
}
