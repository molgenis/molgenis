package org.molgenis.data.blob;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingObject;
import java.nio.channels.ReadableByteChannel;

public abstract class BlobStoreDecorator extends ForwardingObject implements BlobStore {
  private final BlobStore delegateBlobStore;

  BlobStoreDecorator(BlobStore delegateBlobStore) {
    this.delegateBlobStore = requireNonNull(delegateBlobStore);
  }

  @Override
  protected BlobStore delegate() {
    return delegateBlobStore;
  }

  @Override
  public BlobMetadata store(ReadableByteChannel fromChannel) {
    return delegate().store(fromChannel);
  }

  @Override
  public void delete(String blobId) {
    delegate().delete(blobId);
  }

  @Override
  public ReadableByteChannel newChannel(String blobId) {
    return delegate().newChannel(blobId);
  }
}
