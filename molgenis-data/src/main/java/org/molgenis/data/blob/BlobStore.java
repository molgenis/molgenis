package org.molgenis.data.blob;

import java.nio.channels.ReadableByteChannel;

/** Binary large object store */
public interface BlobStore {

  BlobMetadata store(ReadableByteChannel fromChannel);

  void delete(String blobId);

  ReadableByteChannel newChannel(String blobId);
}
