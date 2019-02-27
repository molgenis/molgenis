package org.molgenis.api.files;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
class FileUploaderRegistryImpl implements FileUploaderRegistry {
  private final Map<MimeType, FileUploader> fileUploadServiceMap;

  FileUploaderRegistryImpl() {
    fileUploadServiceMap = new HashMap<>();
  }

  @Override
  public FileUploader getFileUploadService(MimeType mimeType) {
    MimeType unparameterizedMimeType = toUnparameterizedMimeType(mimeType);
    FileUploader fileUploadService = fileUploadServiceMap.get(unparameterizedMimeType);
    if (fileUploadService == null) {
      throw new UnsupportedMimeTypeException(mimeType);
    }
    return fileUploadService;
  }

  @Override
  public void register(FileUploader fileUploadService) {
    fileUploadService
        .getSupportedMimeTypes()
        .forEach(mimeType -> register(mimeType, fileUploadService));
  }

  private MimeType toUnparameterizedMimeType(MimeType mimeType) {
    return new MimeType(mimeType.getType(), mimeType.getSubtype());
  }

  private synchronized void register(MimeType mimeType, FileUploader fileUploadService) {
    MimeType unparameterizedMimeType = toUnparameterizedMimeType(mimeType);
    if (fileUploadServiceMap.containsKey(unparameterizedMimeType)) {
      throw new IllegalArgumentException(
          format(
              "FileUploadService for MIME type '%s' already registered",
              unparameterizedMimeType.toString()));
    }
    fileUploadServiceMap.put(unparameterizedMimeType, fileUploadService);
  }
}
