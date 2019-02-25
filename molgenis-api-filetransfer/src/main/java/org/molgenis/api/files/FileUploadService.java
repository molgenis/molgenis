package org.molgenis.api.filetransfer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.fileupload.FileItemIterator;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.scheduling.annotation.Async;

public interface FileUploadService {
  @Async
  CompletableFuture<List<FileMeta>> upload(FileItemIterator fileItemIterator);
}
