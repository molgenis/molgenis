package org.molgenis.api.filetransfer;

import static java.nio.channels.Channels.newChannel;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.molgenis.data.DataService;
import org.molgenis.data.blob.BlobMetadata;
import org.molgenis.data.blob.BlobStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class FileUploadServiceImpl implements FileUploadService {
  private final BlobStore blobStore;
  private final FileMetaFactory fileMetaFactory;
  private final FileDownloadUriGenerator fileDownloadUriGenerator;
  private final DataService dataService;

  FileUploadServiceImpl(
      BlobStore blobStore,
      FileMetaFactory fileMetaFactory,
      FileDownloadUriGenerator fileDownloadUriGenerator,
      DataService dataService) {
    this.blobStore = requireNonNull(blobStore);
    this.fileMetaFactory = requireNonNull(fileMetaFactory);
    this.fileDownloadUriGenerator = requireNonNull(fileDownloadUriGenerator);
    this.dataService = requireNonNull(dataService);
  }

  @Transactional
  @Override
  public CompletableFuture<List<FileMeta>> upload(FileItemIterator fileItemIterator) {
    List<FileMeta> fileMetaList = new ArrayList<>();
    try {
      while (fileItemIterator.hasNext()) {
        FileItemStream fileItemStream = fileItemIterator.next();
        FileMeta fileMeta = upload(fileItemStream);
        fileMetaList.add(fileMeta);
      }
    } catch (FileUploadException e) {
      throw new UncheckedIOException(new IOException(e));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return CompletableFuture.completedFuture(fileMetaList);
  }

  private FileMeta upload(FileItemStream fileItemStream) throws IOException {
    BlobMetadata blobMetadata;
    try (ReadableByteChannel fromChannel = newChannel(fileItemStream.openStream())) {
      blobMetadata = blobStore.store(fromChannel);
    }

    FileMeta fileMeta = fileMetaFactory.create(blobMetadata.getId());
    fileMeta.setFilename(fileItemStream.getName());
    fileMeta.setContentType(fileItemStream.getContentType());
    fileMeta.setSize(blobMetadata.getSize());
    fileMeta.setUrl(fileDownloadUriGenerator.generateUri(fileMeta.getId()));
    dataService.add(FILE_META, fileMeta);

    return fileMeta;
  }
}
