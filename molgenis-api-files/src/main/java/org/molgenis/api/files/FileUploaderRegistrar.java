package org.molgenis.api.files;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/** Discovers {@link FileUploader} beans and registers them at the {@link FileUploaderRegistry}. */
@Component
public class FileUploaderRegistrar {
  private final FileUploaderRegistry fileUploadServiceRegistry;

  public FileUploaderRegistrar(FileUploaderRegistry fileUploadServiceRegistry) {
    this.fileUploadServiceRegistry = requireNonNull(fileUploadServiceRegistry);
  }

  public void register(ContextRefreshedEvent event) {
    ApplicationContext ctx = event.getApplicationContext();
    Map<String, FileUploader> fileUploadServiceMap = ctx.getBeansOfType(FileUploader.class);
    fileUploadServiceMap.values().forEach(fileUploadServiceRegistry::register);
  }
}
