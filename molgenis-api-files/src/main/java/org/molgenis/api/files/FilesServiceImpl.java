package org.molgenis.api.files;

import static java.nio.channels.Channels.newChannel;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.BlobMetadata;
import org.molgenis.data.file.BlobStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
class FilesServiceImpl implements FilesService {
  private final DataService dataService;
  private final BlobStore blobStore;
  private final FileMetaFactory fileMetaFactory;

  FilesServiceImpl(DataService dataService, BlobStore blobStore, FileMetaFactory fileMetaFactory) {
    this.dataService = requireNonNull(dataService);
    this.blobStore = requireNonNull(blobStore);
    this.fileMetaFactory = requireNonNull(fileMetaFactory);
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

  @Override
  public void delete(String fileId) {
    // FileMetaRepositoryDecorator is responsible for deleting both data and metadata
    dataService.deleteById(FILE_META, fileId);
  }

  @Transactional
  @Override
  public CompletableFuture<FileMeta> upload(HttpServletRequest httpServletRequest) {
    BlobMetadata blobMetadata;
    try (ReadableByteChannel fromChannel = newChannel(httpServletRequest.getInputStream())) {
      blobMetadata = blobStore.store(fromChannel);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    FileMeta fileMeta = createFileMeta(httpServletRequest, blobMetadata);
    dataService.add(FILE_META, fileMeta);
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

  private FileMeta createFileMeta(
      HttpServletRequest httpServletRequest, BlobMetadata blobMetadata) {
    String blobMetadataId = blobMetadata.getId();

    String uriString =
        ServletUriComponentsBuilder.fromRequestUri(httpServletRequest)
            .scheme(null)
            .host(null)
            .port(null)
            .userInfo(null)
            .pathSegment(blobMetadataId)
            .queryParam("alt", "media")
            .build()
            .toUriString();

    String filename = httpServletRequest.getHeader("x-molgenis-filename");
    FileMeta fileMeta = fileMetaFactory.create(blobMetadataId);
    fileMeta.setFilename(filename != null ? filename : "unknown");
    fileMeta.setContentType(httpServletRequest.getContentType());
    fileMeta.setSize(blobMetadata.getSize());
    fileMeta.setUrl(uriString);
    return fileMeta;
  }
}
