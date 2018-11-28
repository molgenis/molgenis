package org.molgenis.navigator.download.job;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.file.FileDownloadController.URI;
import static org.molgenis.data.importer.emx.EmxFileExtensions.XLSX;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ResourceDownloadConfig {

  private final ResourceDownloadService resourceDownloadService;

  public ResourceDownloadConfig(ResourceDownloadService resourceDownloadService) {
    this.resourceDownloadService = requireNonNull(resourceDownloadService);
  }

  @Bean
  public JobFactory<ResourceDownloadJobExecution> downloadJobExecutionJobFactory() {
    return new JobFactory<ResourceDownloadJobExecution>() {
      @Override
      public Job createJob(ResourceDownloadJobExecution downloadJobExecution) {
        String fileType = XLSX.toString().toLowerCase();
        final String filename = getDownloadFilename(fileType);
        downloadJobExecution.setResultUrl(URI + '/' + filename);
        downloadJobExecution.setProgressInt(0);
        downloadJobExecution.setProgressMessage(
            getMessage("progress-download-running", "Starting preparing download."));

        return progress ->
            resourceDownloadService.download(
                downloadJobExecution.getResources(), filename, progress);
      }
    };
  }

  private String getDownloadFilename(String extension) {
    String timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss.SSS"));
    return String.format("%s.%s", timestamp, extension);
  }

  private String getMessage(String key, String defaultMessage) {
    return MessageSourceHolder.getMessageSource()
        .getMessage(key, new Object[] {}, defaultMessage, LocaleContextHolder.getLocale());
  }
}
