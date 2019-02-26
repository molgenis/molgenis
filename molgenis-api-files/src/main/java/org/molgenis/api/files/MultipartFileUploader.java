package org.molgenis.api.files;

import static com.google.common.collect.ImmutableList.of;
import static java.nio.channels.Channels.newChannel;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.molgenis.data.DataService;
import org.molgenis.data.blob.BlobMetadata;
import org.molgenis.data.blob.BlobStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Upload files included in multipart HTTP requests. */
@Service
class MultipartFileUploader implements FileUploader {
  private static final ImmutableList<MimeType> MIME_TYPES =
      of(MimeType.valueOf("multipart/form-data"), MimeType.valueOf("multipart/mixed"));

  private final BlobStore blobStore;
  private final FileMetaFactory fileMetaFactory;
  private final DataService dataService;

  MultipartFileUploader(
      BlobStore blobStore, FileMetaFactory fileMetaFactory, DataService dataService) {
    this.blobStore = requireNonNull(blobStore);
    this.fileMetaFactory = requireNonNull(fileMetaFactory);
    this.dataService = requireNonNull(dataService);
  }

  @Override
  public Collection<MimeType> getSupportedMimeTypes() {
    return MIME_TYPES;
  }

  @Transactional
  @Override
  public FileMeta upload(HttpServletRequest httpServletRequest) {
    FileMeta fileMeta;

    ServletFileUpload servletFileUpload = new ServletFileUpload();
    try {
      FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(httpServletRequest);
      if (!fileItemIterator.hasNext()) {
        throw new IllegalArgumentException("request contains no files to upload");
      }
      fileMeta = upload(fileItemIterator.next(), httpServletRequest);
      if (fileItemIterator.hasNext()) {
        throw new IllegalArgumentException("request contains multiple files to upload");
      }
    } catch (FileUploadException e) {
      throw new UncheckedIOException(new IOException(e));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return fileMeta;
  }

  private FileMeta upload(FileItemStream fileItemStream, HttpServletRequest httpServletRequest) {
    BlobMetadata blobMetadata;
    try (ReadableByteChannel fromChannel = newChannel(fileItemStream.openStream())) {
      blobMetadata = blobStore.store(fromChannel);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    FileMeta fileMeta = createFileMeta(fileItemStream, httpServletRequest, blobMetadata);
    dataService.add(FILE_META, fileMeta);

    return fileMeta;
  }

  private FileMeta createFileMeta(
      FileItemStream fileItemStream,
      HttpServletRequest httpServletRequest,
      BlobMetadata blobMetadata) {
    String blobMetadataId = blobMetadata.getId();

    String uriString =
        ServletUriComponentsBuilder.fromRequestUri(httpServletRequest)
            .pathSegment(blobMetadataId)
            .queryParam("alt", "media")
            .toUriString();

    FileMeta fileMeta = fileMetaFactory.create(blobMetadataId);
    fileMeta.setFilename(fileItemStream.getName());
    fileMeta.setContentType(fileItemStream.getContentType());
    fileMeta.setSize(blobMetadata.getSize());
    fileMeta.setUrl(uriString);
    return fileMeta;
  }
}
