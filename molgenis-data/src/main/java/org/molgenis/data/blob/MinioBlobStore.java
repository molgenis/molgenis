package org.molgenis.data.blob;

import static java.util.Objects.requireNonNull;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.molgenis.data.populate.IdGenerator;

public class MinioBlobStore implements BlobStore {
  private static final String BUCKET_NAME = "molgenis";

  private IdGenerator idGenerator;
  private MinioClient minioClient;

  public MinioBlobStore(IdGenerator idGenerator) {
    this.idGenerator = requireNonNull(idGenerator);
    try {
      minioClient = new MinioClient("http://127.0.0.1:9000", "molgenis", "molgenis");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public WritableBlobChannel newChannel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public BlobMetadata store(ReadableByteChannel fromChannel) {
    String blobId = generateBlobId();
    String contentType = "unknown/content-type";
    try {
      minioClient.putObject(BUCKET_NAME, blobId, Channels.newInputStream(fromChannel), contentType);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    ObjectStat objectStat;
    try {
      objectStat = minioClient.statObject(BUCKET_NAME, blobId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    long size = objectStat.length();

    return BlobMetadata.builder().setId(blobId).setSize(size).build();
  }

  @Override
  public void delete(String blobId) {
    try {
      minioClient.removeObject(BUCKET_NAME, blobId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ReadableByteChannel newChannel(String blobId) {
    try {
      return Channels.newChannel(minioClient.getObject(BUCKET_NAME, blobId));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String generateBlobId() {
    return idGenerator.generateId();
  }
}
