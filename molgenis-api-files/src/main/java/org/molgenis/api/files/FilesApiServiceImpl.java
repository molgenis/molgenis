package org.molgenis.api.files;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.google.common.io.ByteStreams;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.BlobStore;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
class FilesApiServiceImpl implements FilesApiService {
  private final DataService dataService;
  private final BlobStore blobStore;
  private final FileUploaderRegistry fileUploadServiceRegistry;

  FilesApiServiceImpl(
      DataService dataService,
      BlobStore blobStore,
      FileUploaderRegistry fileUploadServiceRegistry) {
    this.dataService = requireNonNull(dataService);
    this.blobStore = requireNonNull(blobStore);
    this.fileUploadServiceRegistry = requireNonNull(fileUploadServiceRegistry);
  }

  @Transactional(readOnly = true)
  @Override
  public FileMeta getFileMeta(String fileId) {
    FileMeta fileMeta = dataService.findOneById(FILE_META, fileId, FileMeta.class);
    if (fileMeta == null) {
      throw new UnknownEntityException(FILE_META, fileId);
    }
    return fileMeta;
  }

  @Transactional
  @Override
  public CompletableFuture<FileMeta> upload(HttpServletRequest httpServletRequest) {
    MimeType mimeType = MimeTypeUtils.parseMimeType(httpServletRequest.getContentType());
    FileUploader fileUploadService = fileUploadServiceRegistry.getFileUploadService(mimeType);
    FileMeta fileMeta = fileUploadService.upload(httpServletRequest);
    return CompletableFuture.completedFuture(fileMeta);
  }

  @Transactional(readOnly = true)
  @Override
  public ResponseEntity<StreamingResponseBody> download(String fileId) {
    FileMeta fileMeta = getFileMeta(fileId);

    ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
    builder.header(CONTENT_TYPE, fileMeta.getContentType());
    builder.header(CONTENT_DISPOSITION, "attachment; filename=\"" + fileMeta.getFilename() + "\"");

    Long contentLength = fileMeta.getSize();
    if (contentLength != null) {
      builder.contentLength(contentLength);
    }

    return builder.body(
        outputStream -> {
          try (ReadableByteChannel fromChannel = blobStore.newChannel(fileId)) {
            ByteStreams.copy(fromChannel, Channels.newChannel(outputStream));
          }
        });
  }
}
