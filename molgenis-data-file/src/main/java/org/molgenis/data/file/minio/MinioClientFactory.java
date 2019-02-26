package org.molgenis.data.file.minio;

import static java.util.Objects.requireNonNull;

import io.minio.MinioClient;
import javax.annotation.Nullable;

class MinioClientFactory {
  private final String bucketName;
  private final String minioEndpoint;
  private final String minioAccessKey;
  private final String minioSecretKey;
  @Nullable private final String region;

  MinioClientFactory(
      String bucketName,
      String endpoint,
      String accessKey,
      String secretKey,
      @Nullable String region) {
    this.bucketName = requireNonNull(bucketName);
    this.minioEndpoint = requireNonNull(endpoint);
    this.minioAccessKey = requireNonNull(accessKey);
    this.minioSecretKey = requireNonNull(secretKey);
    this.region = region;
  }

  MinioClient createClient() {
    MinioClient minioClient;
    try {
      minioClient = new MinioClient(minioEndpoint, minioAccessKey, minioSecretKey, region);
      minioClient.traceOn(System.out);
    } catch (Exception e) {
      throw new RuntimeException(e); // TODO proper exception handling
    }
    return minioClient;
  }
}
