package org.molgenis.data.file.minio;

import static java.util.Objects.requireNonNull;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.xmlpull.v1.XmlPullParserException;

class MinioClientFactoryImpl implements MinioClientFactory {
  private static final Logger LOG = LoggerFactory.getLogger(MinioClientFactoryImpl.class);

  private final String bucketName;
  private final String minioEndpoint;
  private final String minioAccessKey;
  private final String minioSecretKey;
  private final String region;

  MinioClientFactoryImpl(
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

  @Override
  @Retryable(
      value = {IOException.class},
      maxAttempts = 10,
      backoff = @Backoff(delay = 1000, multiplier = 2.0))
  public MinioClient createClient() throws IOException {
    MinioClient minioClient;
    try {
      minioClient = new MinioClient(minioEndpoint, minioAccessKey, minioSecretKey, region);
    } catch (InvalidEndpointException | InvalidPortException e) {
      throw new IllegalArgumentException(e);
    }

    LOG.debug("Connecting to Minio on '{}' ...", minioEndpoint);
    createBucketIfNotExists(minioClient, bucketName, region);
    LOG.info("Connected to Minio on '{}'", minioEndpoint);
    return minioClient;
  }

  private void createBucketIfNotExists(MinioClient minioClient, String bucketName, String region)
      throws IOException {
    try {
      if (!minioClient.bucketExists(bucketName)) {
        LOG.trace("Creating Minio bucket '{}' ...", bucketName);
        minioClient.makeBucket(bucketName, region);
        LOG.debug("Created Minio bucket '{}'", bucketName);
      }
    } catch (IOException e) {
      LOG.error("Failed to connect to Minio on '{}'. Is Minio running?", minioEndpoint);
      throw e;
    } catch (InvalidBucketNameException
        | NoSuchAlgorithmException
        | InsufficientDataException
        | InvalidKeyException
        | NoResponseException
        | XmlPullParserException
        | ErrorResponseException
        | InternalException
        | RegionConflictException e) {
      throw new UncheckedIOException(new IOException(e));
    }
  }
}
