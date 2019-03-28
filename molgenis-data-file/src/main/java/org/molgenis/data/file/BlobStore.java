package org.molgenis.data.file;

import java.nio.channels.ReadableByteChannel;

/**
 * Binary large object store
 *
 * @see BlobMetadata
 */
public interface BlobStore {

  /**
   * Store a binary large object.
   *
   * @throws java.io.UncheckedIOException if an error occurs reading/writing data.
   * @return metadata for the persisted binary data
   */
  BlobMetadata store(ReadableByteChannel fromChannel);

  /**
   * Delete a binary large object
   *
   * @throws java.io.UncheckedIOException if an error occurs reading/writing data.
   */
  void delete(String blobId);

  /**
   * Read a binary large object
   *
   * @throws java.io.UncheckedIOException if an error occurs reading/writing data.
   */
  ReadableByteChannel newChannel(String blobId);
}
