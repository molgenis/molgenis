package org.molgenis.data.blob;

import static java.util.Objects.requireNonNull;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.molgenis.data.populate.IdGenerator;
import org.springframework.beans.factory.annotation.Value;

class MinioBlobStore implements BlobStore {
  private final String bucketName;
  private final IdGenerator idGenerator;
  private final MinioClient minioClient;

  MinioBlobStore(
      @Value("${minio_bucket_name:molgenis}") String bucketName,
      @Value("${minio_endpoint:http://127.0.0.1:9000}") String minioEndpoint,
      @Value("${minio_access_key:molgenis}") String minioAccessKey,
      @Value("${minio_secret_key:molgenis}") String minioSecretKey,
      IdGenerator idGenerator) {
    this.bucketName = requireNonNull(bucketName);
    this.idGenerator = requireNonNull(idGenerator);
    try {
      minioClient = new MinioClient(minioEndpoint, minioAccessKey, minioSecretKey, "eu-central-1");
      minioClient.traceOn(System.out);
    } catch (Exception e) {
      throw new RuntimeException(e); // TODO proper exception handling
    }
  }

  @Override
  public BlobMetadata store(ReadableByteChannel fromChannel) {
    String blobId = generateBlobId();

    // The "octet-stream" subtype is used to indicate that a body contains arbitrary binary data.
    String contentType = "application/octet-stream";
    try {
      minioClient.putObject(bucketName, blobId, Channels.newInputStream(fromChannel), contentType);
    } catch (Exception e) {
      throw new RuntimeException(e); // TODO proper exception handling
    }

    ObjectStat objectStat;
    try {
      objectStat = minioClient.statObject(bucketName, blobId);
    } catch (Exception e) {
      throw new RuntimeException(e); // TODO proper exception handling
    }
    long size = objectStat.length();

    return BlobMetadata.builder().setId(blobId).setSize(size).build();
  }

  @Override
  public void delete(String blobId) {
    try {
      minioClient.removeObject(bucketName, blobId);
    } catch (Exception e) {
      throw new RuntimeException(e); // TODO proper exception handling
    }
  }

  @Override
  public ReadableByteChannel newChannel(String blobId) {
    try {
      return Channels.newChannel(minioClient.getObject(bucketName, blobId));
    } catch (Exception e) {
      throw new RuntimeException(e); // TODO proper exception handling
    }
  }

  private String generateBlobId() {
    return idGenerator.generateId();
  }
}
