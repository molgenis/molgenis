package org.molgenis.data.file;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaMetadata;
import org.springframework.stereotype.Component;

@Component
public class FileMetaRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<FileMeta, FileMetaMetadata> {
  private final FileStore fileStore;
  private final BlobStore blobStore;

  public FileMetaRepositoryDecoratorFactory(
      FileMetaMetadata fileMetaMetadata, FileStore fileStore, BlobStore blobStore) {
    super(fileMetaMetadata);
    this.fileStore = requireNonNull(fileStore);
    this.blobStore = requireNonNull(blobStore);
  }

  @Override
  public Repository<FileMeta> createDecoratedRepository(Repository<FileMeta> repository) {
    return new FileMetaRepositoryDecorator(repository, fileStore, blobStore);
  }
}
