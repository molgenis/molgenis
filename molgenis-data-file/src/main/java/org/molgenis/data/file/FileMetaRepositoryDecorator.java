package org.molgenis.data.file;

import static java.util.Objects.requireNonNull;

import java.io.UncheckedIOException;
import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.model.FileMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Repository decorator that updates {@link FileStore} on {@link FileMeta} changes. */
public class FileMetaRepositoryDecorator extends AbstractRepositoryDecorator<FileMeta> {
  private static final Logger LOG = LoggerFactory.getLogger(FileMetaRepositoryDecorator.class);

  private final FileStore fileStore;
  private final BlobStore blobStore;

  public FileMetaRepositoryDecorator(
      Repository<FileMeta> delegateRepository, FileStore fileStore, BlobStore blobStore) {
    super(delegateRepository);
    this.fileStore = requireNonNull(fileStore);
    this.blobStore = requireNonNull(blobStore);
  }

  @Override
  public void delete(FileMeta fileMeta) {
    deleteFile(fileMeta);
    super.delete(fileMeta);
  }

  @Override
  public void deleteById(Object id) {
    deleteFile(getFileMeta(id));
    super.deleteById(id);
  }

  @Override
  public void deleteAll() {
    query().findAll().forEach(this::deleteFile);
    super.deleteAll();
  }

  @Override
  public void delete(Stream<FileMeta> fileMetaStream) {
    super.delete(
        fileMetaStream.filter(
            fileMeta -> {
              this.deleteFile(fileMeta);
              return true;
            }));
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    super.deleteAll(
        ids.filter(
            id -> {
              this.deleteFile(getFileMeta(id));
              return true;
            }));
  }

  private void deleteFile(FileMeta fileMeta) {
    if (isBlobStoreFile(fileMeta)) {
      deleteFileFromBlobStore(fileMeta);
    } else {
      deleteFileFromFileStore(fileMeta);
    }
  }

  private boolean isBlobStoreFile(FileMeta fileMeta) {
    return fileMeta.getUrl().endsWith("alt=media");
  }

  private void deleteFileFromBlobStore(FileMeta fileMeta) {
    try {
      blobStore.delete(fileMeta.getId());
    } catch (UncheckedIOException e) {
      LOG.warn("Could not delete file '{}' from blob store", fileMeta.getId());
    }
  }

  private void deleteFileFromFileStore(FileMeta fileMeta) {
    try {
      fileStore.delete(fileMeta.getId());
    } catch (UncheckedIOException e) {
      LOG.warn("Could not delete file '{}' from file store", fileMeta.getId());
    }
  }

  private FileMeta getFileMeta(Object id) {
    FileMeta fileMeta = findOneById(id);
    if (fileMeta == null) {
      throw new UnknownEntityException(getEntityType(), id);
    }
    return fileMeta;
  }
}
