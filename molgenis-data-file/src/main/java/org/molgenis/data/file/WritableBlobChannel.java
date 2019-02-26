package org.molgenis.data.file;

import com.google.auto.value.AutoValue;
import java.nio.channels.WritableByteChannel;

@AutoValue
public abstract class WritableBlobChannel {
  public abstract String getBlobId();

  public abstract WritableByteChannel getWritableByteChannel();

  public static WritableBlobChannel create(String blobId, WritableByteChannel writableByteChannel) {
    return new AutoValue_WritableBlobChannel(blobId, writableByteChannel);
  }
}
